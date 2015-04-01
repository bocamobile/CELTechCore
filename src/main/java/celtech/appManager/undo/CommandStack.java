/*
 * Copyright 2015 CEL UK
 */
package celtech.appManager.undo;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * The CommandStack is a stack of Commands that has an index into the current stack position. When
 * an undo is applied the Command at the current index is undone, and the index is reduced by 1.
 *
 * @author tony
 */
public class CommandStack
{

    private BooleanProperty canUndo = new SimpleBooleanProperty();
    private BooleanProperty canRedo = new SimpleBooleanProperty();

    public class UndoException extends Exception
    {

        public UndoException(String message)
        {
            super(message);
        }
    }

    public CommandStack()
    {
        commands = FXCollections.observableArrayList();
        canUndo.bind(index.greaterThan(-1));
        canRedo.bind(Bindings.size(commands).greaterThan(index.add(1)));
    }

    private ObservableList<Command> commands;

    /**
     * The position of the last command to be performed.
     */
    private IntegerProperty index = new SimpleIntegerProperty(-1);

    public void do_(Command command)
    {
        System.out.println("Do COMMAND " + command);
        clearEndOfList();
        commands.add(command);
        command.do_();
        index.set(index.get() + 1);
        tryMerge();
    }
    
    /**
     * Clear all list entries that are after the current index.
     */
    private void clearEndOfList() {
        commands.subList(index.get() + 1, commands.size()).clear();
    }

    public void undo() throws UndoException
    {
        if (canUndo.not().get())
        {
            throw new UndoException("Cannot undo - nothing to undo");
        }
        Command currentCommand = commands.get(index.get());
        currentCommand.undo();
        index.set(index.get() - 1);
    }

    public void redo() throws UndoException
    {
        if (canRedo.not().get())
        {
            throw new UndoException("Cannot redo - nothing to redo");
        }
        Command followingCommand = commands.get(index.get() + 1);
        followingCommand.redo();
        index.set(index.get() + 1);
    }

    public ReadOnlyBooleanProperty getCanRedo()
    {
        return canRedo;
    }

    public ReadOnlyBooleanProperty getCanUndo()
    {
        return canUndo;
    }

    /**
     * Try to merge the previous command with the last run command.
     */
    private void tryMerge()
    {
        if (index.get() < 1) {
            return;
        }
        Command lastCommand = commands.get(index.get());
        Command previousCommand = commands.get(index.get() - 1);
        if (previousCommand.canMergeWith(lastCommand)) {
            previousCommand.merge(lastCommand);
            // remove last command from list
            commands.remove(commands.size() - 1);
            index.set(index.get() - 1);
        }
    }

}
