package celtech.utils.tasks;

import javafx.concurrent.Task;

/**
 *
 * @author Ian
 */
public class TestTaskExecutor implements TaskExecutor
{

    @Override
    public void respondOnGUIThread(TaskResponder responder, boolean success, String message)
    {
        TaskResponse taskResponse = new TaskResponse(message);
        taskResponse.setSucceeded(success);

        responder.taskEnded(taskResponse);
    }

    @Override
    public void respondOnGUIThread(TaskResponder responder, boolean success, String message,
        Object returnedObject)
    {
        TaskResponse taskResponse = new TaskResponse(message);
        taskResponse.setSucceeded(success);
        taskResponse.setReturnedObject(returnedObject);

        responder.taskEnded(taskResponse);
    }

    @Override
    public void respondOnCurrentThread(TaskResponder responder, boolean success, String message)
    {
        TaskResponse taskResponse = new TaskResponse(message);
        taskResponse.setSucceeded(success);

        responder.taskEnded(taskResponse);
    }

    @Override
    public void runOnGUIThread(Runnable runnable)
    {
        runnable.run();
    }
    
    @Override
    public void runOnBackgroundThread(Runnable runnable)
    {
        runnable.run();
    }    

    @Override
    public void runAsTask(NoArgsVoidFunc action, NoArgsVoidFunc successHandler,
        NoArgsVoidFunc failureHandler, String taskName)
    {
        try
        {
            action.run();
            successHandler.run();

        } catch (Exception ex)
        {
            ex.printStackTrace();
            try
            {
                failureHandler.run();
            } catch (Exception ex1)
            {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void runTaskAsDaemon(Task task)
    {
    }
}
