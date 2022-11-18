package org.mersic.arithmeticcoder;

import org.junit.jupiter.api.Test;
import org.mersic.arithmeticcoder.model.AdaptiveModel;
import org.mersic.arithmeticcoder.model.Model;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestNoAdaptiveModelReuse {

    @Test
    public void test_AdaptiveModel_can_not_be_reused() {
        int[] chars = new int[] {'0','1', '2', '3', '4', '5', '6', '7', '8', '9'};
        assertThrows(IllegalStateException.class, () -> {
            Model model = new AdaptiveModel.Builder().chars(chars).build();
            model.startEncode();
            model.startDecode();
        });
        assertThrows(IllegalStateException.class, () -> {
            Model model = new AdaptiveModel.Builder().chars(chars).build();
            model.startDecode();
            model.startEncode();
        });
        assertThrows(IllegalStateException.class, () -> {
            Model model = new AdaptiveModel.Builder().chars(chars).build();
            model.startEncode();
            model.startEncode();
        });
        assertThrows(IllegalStateException.class, () -> {
            Model model = new AdaptiveModel.Builder().chars(chars).build();
            model.startDecode();
            model.startDecode();
        });
        
    }
}
