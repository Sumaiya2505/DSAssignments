public class LamportClock
{
        private int currentTime;

        public LamportClock() {
            this.currentTime = 0;
        }

        public synchronized int getTime() {
        return currentTime;
         }

        public synchronized void increaseTime() {
            currentTime++;
        }

        public synchronized void updateTime(int receivedTime) {
            currentTime = Math.max(currentTime, receivedTime) + 1;
        }


}



