package celtech.utils.tasks;

import celtech.printerControl.model.HardwarePrinter;
import javafx.application.Platform;
import javafx.concurrent.Task;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;

/**
 *
 * @author Ian
 */
public class LiveTaskExecutor implements TaskExecutor
{

    private final Stenographer steno = StenographerFactory.getStenographer(
        HardwarePrinter.class.getName());

    @Override
    public void runTaskAsDaemon(Task task)
    {
        runOnGUIThread(new Runnable()
        {

            @Override
            public void run()
            {
                Thread th = new Thread(task);
                th.setDaemon(true);
                th.start();
            }
        });
    }

    @Override
    public void runOnGUIThread(Runnable runnable)
    {
        if (Platform.isFxApplicationThread())
        {
            runnable.run();
        } else
        {
            Platform.runLater(runnable);
        }
    }

    @Override
    public void respondOnGUIThread(TaskResponder responder, boolean success, String message)
    {
        respondOnGUIThread(responder, success, message, null);
    }

    @Override
    public void respondOnGUIThread(TaskResponder responder, boolean success, String message,
        Object returnedObject)
    {
        if (responder != null)
        {
            TaskResponse taskResponse = new TaskResponse(message);
            taskResponse.setSucceeded(success);

            if (returnedObject != null)
            {
                taskResponse.setReturnedObject(returnedObject);
            }

            Platform.runLater(() ->
            {
                responder.taskEnded(taskResponse);
            });
        }
    }

    @Override
    public void respondOnCurrentThread(TaskResponder responder, boolean success, String message)
    {
        if (responder != null)
        {
            TaskResponse taskResponse = new TaskResponse(message);
            taskResponse.setSucceeded(success);

            responder.taskEnded(taskResponse);
        }
    }

    @Override
    public void runAsTask(NoArgsVoidFunc action, NoArgsVoidFunc successHandler,
        NoArgsVoidFunc failureHandler, String taskName)
    {
        Runnable runTask = () ->
        {
            try
            {
                action.run();
                successHandler.run();

            } catch (Exception ex)
            {
                ex.printStackTrace();
                steno.error("Failure running task: " + ex);
                try
                {
                    if (failureHandler != null)
                    {
                        failureHandler.run();
                    } else
                    {
                        steno.warning("No failure handler for this case");
                    }
                } catch (Exception ex1)
                {
                    steno.error("Error running failure handler!: " + ex);
                }
            }
        };
        Thread taskThread = new Thread(runTask);
        // Setting to Daemon is not strictly necessary if the cancelling logic
        // is implemented correctly, but just in case.
        taskThread.setDaemon(true);
        taskThread.setName(taskName);
        taskThread.start();
    }
}
