// websocket-load-tester.ts
import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { performance } from 'perf_hooks';
import fs from 'fs';
import path from 'path';
import * as readline from 'readline';

interface TestConfig {
  serverUrl: string;
  totalConnections: number;
  rampUpTime: number; // in seconds
  testDuration: number; // in seconds
  messageInterval: number; // in seconds
  warmUpConnections: number;
  maxRetries: number;
  subscriptionTypes: Array<{
    destination: string;
    pattern: string; // e.g., "/topic/match/{id}"
    count: number;
  }>;
}

interface ConnectionStats {
  id: string;
  connectedAt: number;
  disconnectedAt: number | null;
  messagesReceived: number;
  errors: string[];
  subscriptionTypes: string[];
  lastMessageTime: number;
  connectionTime: number;
  totalBytesReceived: number;
}

interface TestMetrics {
  totalConnectionsAttempted: number;
  successfulConnections: number;
  failedConnections: number;
  totalMessagesReceived: number;
  totalBytesTransferred: number;
  averageConnectionTime: number;
  p95ConnectionTime: number;
  p99ConnectionTime: number;
  messagesPerSecond: number;
  errorRate: number;
  startTime: number;
  endTime: number;
  connectionTimes: number[];
  subscriptionCounts: Map<string, number>;
}

class WebSocketLoadTester {
  private config: TestConfig;
  private connections: Map<string, Client> = new Map();
  private stats: Map<string, ConnectionStats> = new Map();
  private metrics: TestMetrics;
  private isRunning = false;
  private messageCounter = 0;
  private subscriptionCallbacks: Map<string, (msg: IMessage) => void> = new Map();

  constructor(config: Partial<TestConfig> = {}) {
    this.config = {
      serverUrl: 'http://localhost:8080/ws',
      totalConnections: 1000,
      rampUpTime: 30,
      testDuration: 300,
      messageInterval: 1,
      warmUpConnections: 10,
      maxRetries: 3,
      subscriptionTypes: [
        { destination: '/topic/match/{id}', pattern: '/topic/match/{id}', count: 1000 },
      ],
      ...config,
    };

    this.metrics = this.initializeMetrics();
  }

  private initializeMetrics(): TestMetrics {
    return {
      totalConnectionsAttempted: 0,
      successfulConnections: 0,
      failedConnections: 0,
      totalMessagesReceived: 0,
      totalBytesTransferred: 0,
      averageConnectionTime: 0,
      p95ConnectionTime: 0,
      p99ConnectionTime: 0,
      messagesPerSecond: 0,
      errorRate: 0,
      startTime: 0,
      endTime: 0,
      connectionTimes: [],
      subscriptionCounts: new Map(),
    };
  }

  private generateConnectionId(index: number): string {
    return `conn-${index.toString().padStart(6, '0')}`;
  }

  private getRandomDestination(subscriptionType: any): string {
    const matchId = 'f9b5b0a5-9ecf-4183-8194-b29dd8c2d058';
    const submissionId = `sub-${Math.random().toString(36).substr(2, 9)}`;
    
    return subscriptionType.destination
      .replace('{id}', matchId.toString())
      .replace('{submissionId}', submissionId);
  }

  private createStompClient(connectionId: string, retryCount = 0): Promise<Client> {
    return new Promise((resolve, reject) => {
      const startTime = performance.now();
      
      const client = new Client({
        webSocketFactory: () => new SockJS(this.config.serverUrl),
        reconnectDelay: 0, // Disable auto-reconnect for testing
        heartbeatIncoming: 0,
        heartbeatOutgoing: 0,
        
        onConnect: () => {
          const connectionTime = performance.now() - startTime;
          
          this.stats.set(connectionId, {
            id: connectionId,
            connectedAt: Date.now(),
            disconnectedAt: null,
            messagesReceived: 0,
            errors: [],
            subscriptionTypes: [],
            lastMessageTime: Date.now(),
            connectionTime,
            totalBytesReceived: 0,
          });

          this.metrics.successfulConnections++;
          this.metrics.connectionTimes.push(connectionTime);
          
          console.log(`✅ ${connectionId} connected in ${connectionTime.toFixed(2)}ms`);
          resolve(client);
        },

        onStompError: (frame) => {
          const errorMsg = `STOMP Error: ${frame.headers.message}`;
          console.error(`❌ ${connectionId} failed: ${errorMsg}`);
          
          if (retryCount < this.config.maxRetries) {
            console.log(`🔄 Retrying ${connectionId} (${retryCount + 1}/${this.config.maxRetries})`);
            setTimeout(() => {
              this.createStompClient(connectionId, retryCount + 1)
                .then(resolve)
                .catch(reject);
            }, 1000);
          } else {
            this.metrics.failedConnections++;
            reject(new Error(`Failed to connect after ${this.config.maxRetries} retries`));
          }
        },

        onDisconnect: () => {
          const stats = this.stats.get(connectionId);
          if (stats) {
            stats.disconnectedAt = Date.now();
          }
          console.log(`🔌 ${connectionId} disconnected`);
        },
      });

      client.activate();
      
      // Set timeout for connection
      setTimeout(() => {
        if (!client.connected) {
          client.deactivate();
          reject(new Error('Connection timeout'));
        }
      }, 10000);
    });
  }

  private setupSubscriptions(client: Client, connectionId: string, connectionIndex: number): void {
    let subscriptionsCreated = 0;
    
    this.config.subscriptionTypes.forEach((subscriptionType, typeIndex) => {
      // Distribute subscriptions across connections
      if (connectionIndex % Math.ceil(this.config.totalConnections / subscriptionType.count) === typeIndex) {
        const destination = this.getRandomDestination(subscriptionType);
        
        const callbackKey = `${connectionId}-${destination}`;
        const messageCallback = (message: IMessage) => {
          this.handleMessage(connectionId, message);
        };
        
        this.subscriptionCallbacks.set(callbackKey, messageCallback);
        
        const subscription = client.subscribe(destination, messageCallback);
        
        const stats = this.stats.get(connectionId);
        if (stats) {
          stats.subscriptionTypes.push(destination);
        }
        
        // Track subscription counts
        const currentCount = this.metrics.subscriptionCounts.get(destination) || 0;
        this.metrics.subscriptionCounts.set(destination, currentCount + 1);
        
        subscriptionsCreated++;
        
        console.log(`📡 ${connectionId} subscribed to ${destination}`);
      }
    });

    if (subscriptionsCreated === 0) {
      // Fallback: subscribe to at least one random destination
      const subscriptionType = this.config.subscriptionTypes[0];
      const destination = this.getRandomDestination(subscriptionType);
      client.subscribe(destination, (message: IMessage) => {
        this.handleMessage(connectionId, message);
      });
      
      const stats = this.stats.get(connectionId);
      if (stats) {
        stats.subscriptionTypes.push(destination);
      }
    }
  }

  private handleMessage(connectionId: string, message: IMessage): void {
    const stats = this.stats.get(connectionId);
    if (stats) {
      stats.messagesReceived++;
      stats.lastMessageTime = Date.now();
      stats.totalBytesReceived += message.binaryBody?.length || message.body.length || 0;
    }

    this.metrics.totalMessagesReceived++;
    this.metrics.totalBytesTransferred += message.binaryBody?.length || message.body.length || 0;
    
    this.messageCounter++;
    
    // Log every 1000 messages
    if (this.messageCounter % 1000 === 0) {
      console.log(`📨 Total messages received: ${this.messageCounter}`);
    }
  }

  private async createConnections(): Promise<void> {
    console.log(`🚀 Starting connection ramp-up (${this.config.totalConnections} connections)`);
    
    const connectionsPerSecond = this.config.totalConnections / this.config.rampUpTime;
    const batchSize = Math.max(1, Math.floor(connectionsPerSecond));
    
    for (let i = 0; i < this.config.totalConnections; i += batchSize) {
      const currentBatch = Math.min(batchSize, this.config.totalConnections - i);
      const batchPromises: Promise<void>[] = [];
      
      console.log(`📊 Creating batch ${i / batchSize + 1}: ${currentBatch} connections`);
      
      for (let j = 0; j < currentBatch; j++) {
        const connectionIndex = i + j;
        const connectionId = this.generateConnectionId(connectionIndex);
        
        this.metrics.totalConnectionsAttempted++;
        
        const connectionPromise = this.createStompClient(connectionId)
          .then(client => {
            this.connections.set(connectionId, client);
            this.setupSubscriptions(client, connectionId, connectionIndex);
          })
          .catch(error => {
            console.error(`Failed to create connection ${connectionId}:`, error.message);
          });
        
        batchPromises.push(connectionPromise);
      }
      
      await Promise.all(batchPromises);
      
      // Wait before next batch based on ramp-up rate
      if (i + batchSize < this.config.totalConnections) {
        await new Promise(resolve => setTimeout(resolve, 1000 / connectionsPerSecond * batchSize));
      }
    }
    
    console.log(`✅ All ${this.config.totalConnections} connections established`);
  }

  private calculateMetrics(): void {
    const connectionTimes = this.metrics.connectionTimes;
    if (connectionTimes.length > 0) {
      connectionTimes.sort((a, b) => a - b);
      
      this.metrics.averageConnectionTime = 
        connectionTimes.reduce((sum, time) => sum + time, 0) / connectionTimes.length;
      
      this.metrics.p95ConnectionTime = 
        connectionTimes[Math.floor(connectionTimes.length * 0.95)];
      this.metrics.p99ConnectionTime = 
        connectionTimes[Math.floor(connectionTimes.length * 0.99)];
    }
    
    const testDuration = (this.metrics.endTime - this.metrics.startTime) / 1000;
    this.metrics.messagesPerSecond = this.metrics.totalMessagesReceived / testDuration;
    this.metrics.errorRate = this.metrics.failedConnections / this.metrics.totalConnectionsAttempted;
  }

  private generateReport(): void {
    console.log('\n' + '='.repeat(80));
    console.log('📊 WEBSOCKET LOAD TEST REPORT');
    console.log('='.repeat(80));
    
    console.log(`\nTest Configuration:`);
    console.log(`  Server URL:          ${this.config.serverUrl}`);
    console.log(`  Total Connections:   ${this.config.totalConnections}`);
    console.log(`  Test Duration:       ${this.config.testDuration}s`);
    console.log(`  Ramp-up Time:        ${this.config.rampUpTime}s`);
    
    console.log(`\nConnection Metrics:`);
    console.log(`  Attempted:           ${this.metrics.totalConnectionsAttempted}`);
    console.log(`  Successful:          ${this.metrics.successfulConnections}`);
    console.log(`  Failed:              ${this.metrics.failedConnections}`);
    console.log(`  Success Rate:        ${((this.metrics.successfulConnections / this.metrics.totalConnectionsAttempted) * 100).toFixed(2)}%`);
    
    console.log(`\nPerformance Metrics:`);
    console.log(`  Avg Connection Time: ${this.metrics.averageConnectionTime.toFixed(2)}ms`);
    console.log(`  P95 Connection Time: ${this.metrics.p95ConnectionTime.toFixed(2)}ms`);
    console.log(`  P99 Connection Time: ${this.metrics.p99ConnectionTime.toFixed(2)}ms`);
    console.log(`  Total Messages:      ${this.metrics.totalMessagesReceived}`);
    console.log(`  Messages/Sec:        ${this.metrics.messagesPerSecond.toFixed(2)}`);
    console.log(`  Data Transferred:    ${(this.metrics.totalBytesTransferred / 1024 / 1024).toFixed(2)} MB`);
    
    console.log(`\nSubscription Distribution:`);
    this.metrics.subscriptionCounts.forEach((count, destination) => {
      console.log(`  ${destination}: ${count} subscriptions`);
    });
    
    console.log(`\nConnection Statistics:`);
    let totalMessages = 0;
    let maxMessages = 0;
    let minMessages = Infinity;
    
    this.stats.forEach(stats => {
      totalMessages += stats.messagesReceived;
      maxMessages = Math.max(maxMessages, stats.messagesReceived);
      minMessages = Math.min(minMessages, stats.messagesReceived);
    });
    
    const avgMessagesPerConnection = totalMessages / this.stats.size;
    console.log(`  Avg Msgs/Connection: ${avgMessagesPerConnection.toFixed(2)}`);
    console.log(`  Max Msgs/Connection: ${maxMessages}`);
    console.log(`  Min Msgs/Connection: ${minMessages}`);
    
    console.log('\n' + '='.repeat(80));
  }

  public async run(): Promise<void> {
    if (this.isRunning) {
      throw new Error('Test is already running');
    }
    
    this.isRunning = true;
    this.metrics.startTime = Date.now();
    
    try {
      console.log('🚀 Starting WebSocket Load Test...');
      
      // Step 1: Warm-up phase
      console.log('\n🔥 Warm-up phase...');
      const warmUpConfig = { ...this.config, totalConnections: this.config.warmUpConnections };
      const warmUpTester = new WebSocketLoadTester(warmUpConfig);
      await warmUpTester.createConnections();
      await new Promise(resolve => setTimeout(resolve, 5000));
      warmUpTester.cleanup();
      
      // Step 2: Create all connections
      await this.createConnections();
      
      // Step 3: Run test for specified duration
      console.log(`\n⏱️  Running test for ${this.config.testDuration} seconds...`);
      await new Promise(resolve => setTimeout(resolve, this.config.testDuration * 1000));
      
      // Step 4: Calculate metrics and generate report
      this.metrics.endTime = Date.now();
      this.calculateMetrics();
      this.generateReport();
      
      // Step 5: Save results
      
    } catch (error) {
      console.error('Test failed with error:', error);
    } finally {
      await this.cleanup();
      this.isRunning = false;
    }
  }

  public async cleanup(): Promise<void> {
    console.log('\n🧹 Cleaning up connections...');
    
    const disconnectPromises = Array.from(this.connections.values()).map(client => {
      return new Promise<void>(resolve => {
        if (client.connected) {
          client.deactivate();
        }
        resolve();
      });
    });
    
    await Promise.all(disconnectPromises);
    this.connections.clear();
    this.subscriptionCallbacks.clear();
  }
}

// CLI Interface
async function main() {

  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });

  function question(query: string): Promise<string> {
    return new Promise(resolve => {
      rl.question(query, resolve);
    });
  }

  console.log('🎯 STOMP WebSocket Load Tester');
  console.log('='.repeat(40));

  const serverUrl = await question('Server URL [http://localhost:8080/ws]: ') || 'http://localhost:8080/ws';
  const totalConnections = parseInt(await question('Number of connections [1000]: ') || '1000');
  const testDuration = parseInt(await question('Test duration (seconds) [300]: ') || '300');
  const rampUpTime = parseInt(await question('Ramp-up time (seconds) [30]: ') || '30');

  const config: TestConfig = {
    serverUrl,
    totalConnections,
    testDuration,
    rampUpTime,
    messageInterval: 1,
    warmUpConnections: Math.min(10, Math.floor(totalConnections * 0.01)),
    maxRetries: 3,
    subscriptionTypes: [
      { destination: '/topic/match/{id}', pattern: '/topic/match/{id}', count: Math.floor(totalConnections * 0.5) },
      { destination: '/topic/submission-result/{id}', pattern: '/topic/submission-result/{id}', count: Math.floor(totalConnections * 0.3) },
      { destination: '/topic/match/{id}/countdown', pattern: '/topic/match/{id}/countdown', count: Math.floor(totalConnections * 0.2) },
    ],
  };

  rl.close();

  const tester = new WebSocketLoadTester(config);
  
  // Handle graceful shutdown
  process.on('SIGINT', async () => {
    console.log('\n\n⚠️  Received SIGINT. Shutting down gracefully...');
    await tester.cleanup();
    process.exit(0);
  });

  await tester.run();
}

main().catch(console.error);


export { WebSocketLoadTester, type TestConfig };
