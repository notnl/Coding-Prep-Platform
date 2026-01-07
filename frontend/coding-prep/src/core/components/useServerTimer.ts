import { useState, useEffect } from 'react';


export const useServerTimer = (startTime: number | null,dur?: number|null) => {

  const [, setTick] = useState(0);

  useEffect(() => {
    if (startTime === null) {
      return;
    }
    const timerInterval = setInterval(() => {     
      setTick(prevTick => prevTick + 1);
    }, 500);

    return () => clearInterval(timerInterval);
  }, [startTime]);

  if (startTime === null) {
    return { minutes: 0, seconds: 0, totalSeconds: 0, isFinished: false };
  }

  var remainingTime = startTime - Date.now(); // StartTime is the scheduled epoch millisecond that will start the game
  if (dur != null) { 
      remainingTime = (startTime + dur) - Date.now() // if duration exists then we should add onto the startTime instead
  }

  const finalRemainingTime = remainingTime > 0 ? remainingTime : 0;

  const totalSeconds = Math.floor(finalRemainingTime / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  const isFinished = finalRemainingTime <= 0;

  return {
    minutes,
    seconds,
    totalSeconds,
    isFinished,
  };
};

