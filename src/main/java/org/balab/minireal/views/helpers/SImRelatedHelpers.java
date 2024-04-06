package org.balab.minireal.views.helpers;

import org.springframework.stereotype.Component;

@Component
public class SImRelatedHelpers
{
    public Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName))
                return t;
        }
        return null;
    }

    public void interruptThread(Thread thread)
    {
        if(thread != null){
            thread.stop();
//            thread.interrupt();
            System.out.println("Thread deleted");
        }
    }

}
