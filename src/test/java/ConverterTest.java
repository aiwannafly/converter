import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

class ConverterTest {
    Collection<Converter.TreeEntity> entities;
    List<String> expectedTrees;

    @BeforeEach
    void initExample() {
        entities = List.of(
                new Converter.TreeEntity(1, "1", null),
                new Converter.TreeEntity(2, "2", 1),
                new Converter.TreeEntity(3, "3", 1),
                new Converter.TreeEntity(7, "7", 2),
                new Converter.TreeEntity(8, "8", 2),
                new Converter.TreeEntity(4, "4", 3),
                new Converter.TreeEntity(5, "5", 3),
                new Converter.TreeEntity(6, "6", 3),
                new Converter.TreeEntity(9, "9", 7),
                new Converter.TreeEntity(20, "20", null),
                new Converter.TreeEntity(21, "21", 20)
        );
        expectedTrees = List.of(
                "1 3 6 5 4 2 8 7 9 ",
                "20 21 "
        );
    }

    @Test
    void convertWithMemory() {
        testConvert(Converter::convertWithMemory);
    }

    @Test
    void convertWithNoMemory() {
        testConvert(Converter::convertWithNoMemory);
    }

    @Test
    void convertWithOrder() {
        testConvert(Converter::convertOrdered);
    }

    void testConvert(ConverterApi converter) {
        Collection<Converter.TreeDTO> res = converter.convertWithMemory(entities);
        List<String> resultedTrees = res.stream().map(this::treeToString).toList();
        Assertions.assertIterableEquals(resultedTrees, expectedTrees);
    }

    String treeToString(Converter.TreeDTO root) {
        Deque<Converter.TreeDTO> stack = new ArrayDeque<>();
        stack.addLast(root);
        StringBuilder builder = new StringBuilder();
        while (!stack.isEmpty()) {
            Converter.TreeDTO current = stack.pollLast();
            builder.append(current.getName()).append(" ");
            for (Converter.TreeDTO child : current.getChildren()) {
                stack.addLast(child);
            }
        }
        return builder.toString();
    }

    private interface ConverterApi {
        Collection<Converter.TreeDTO> convertWithMemory(Collection<Converter.TreeEntity> entities);
    }
}
