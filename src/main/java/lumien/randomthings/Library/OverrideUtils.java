package lumien.randomthings.Library;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class OverrideUtils {
    public static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }

    public static Class[] convertToClassArray(Object[] objectArray) {
        Class[] classArray = new Class[objectArray.length];
        for (int i = 0; i < objectArray.length; i++) {
            classArray[i] = objectArray[i].getClass();
        }
        return classArray;
    }
}
