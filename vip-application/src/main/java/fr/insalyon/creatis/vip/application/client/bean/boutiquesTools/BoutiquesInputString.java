package fr.insalyon.creatis.vip.application.client.bean.boutiquesTools;

import java.util.List;
import java.util.Map;

/**
 * Representation of a String or File input in an application Boutiques descriptor
 *
 * @author Guillaume Vanel
 * @version %I%, %G%
 */
public class BoutiquesInputString extends BoutiquesInput{
    private final String defaultValue;

    /**
     * @param id                    String
     * @param name                  String
     * @param description           String
     * @param type                  InputType.STRING or InputType.FILE
     * @param isOptional            boolean
     * @param disablesInputsId      List of String IDs of inputs disabled when this is non-empty
     * @param requiresInputsId      List of String IDs of inputs requiring this to be non-empty
     * @param possibleValues        List of String values this input can take
     * @param valueDisablesInputsId Map from String values to Lists of String IDs of inputs disabled by corresponding
     *                              value
     * @param valueRequiresInputsId Map from String values to Lists of String IDs of inputs requiring corresponding
     *                              value
     * @param defaultValue          String
     * @throws RuntimeException if descriptor is invalid
     */
    public BoutiquesInputString(String id, String name, String description, InputType type, boolean isOptional,
                                List<String> disablesInputsId, List<String> requiresInputsId,
                                List<String> possibleValues, Map<String, List<String>> valueDisablesInputsId,
                                Map<String, List<String>> valueRequiresInputsId, String defaultValue) throws RuntimeException{
        super(id, name, description, type, isOptional, disablesInputsId, requiresInputsId, possibleValues,
                valueDisablesInputsId, valueRequiresInputsId);
        this.defaultValue = defaultValue;
    }

    /**
     * @return String representing this input's default value, or null if there is not any
     */
    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }
}