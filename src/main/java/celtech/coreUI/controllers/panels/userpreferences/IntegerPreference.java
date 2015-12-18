package celtech.coreUI.controllers.panels.userpreferences;

import celtech.Lookup;
import celtech.coreUI.components.RestrictedNumberField;
import celtech.coreUI.controllers.panels.PreferencesInnerPanelController;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;

/**
 *
 * @author Ian
 */
public class IntegerPreference implements PreferencesInnerPanelController.Preference
{

    private final RestrictedNumberField control;
    private final IntegerProperty integerProperty;
    private final String caption;

    public IntegerPreference(IntegerProperty integerProperty,
            String caption)
    {
        this.integerProperty = integerProperty;
        this.caption = caption;

        control = new RestrictedNumberField();
        control.setPrefWidth(150);
        control.setMinWidth(control.getPrefWidth());
        control.setAllowedDecimalPlaces(0);
        control.setAllowNegative(false);
        control.setMaxLength(4);
        control.floatValueProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) ->
        {
            updateValueFromControl();
        });
    }

    @Override
    public void updateValueFromControl()
    {
        integerProperty.set(control.intValueProperty().get());

        // User Preferences controls whether the property can be set - read back just in case our selection was overridden
        control.intValueProperty().set(integerProperty.get());
    }

    @Override
    public void populateControlWithCurrentValue()
    {
        control.floatValueProperty().set(integerProperty.get());
    }

    @Override
    public Control getControl()
    {
        return control;
    }

    @Override
    public String getDescription()
    {
        return Lookup.i18n(caption);
    }
}