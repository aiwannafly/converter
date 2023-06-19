import lombok.*;

import java.util.*;

public class Converter {

    public static Collection<TreeDTO> convertWithMemory(Collection<TreeEntity> entities) {
        Collection<TreeDTO> rootNodes = new ArrayList<>();
        Map<Integer, TreeDTO> dtoMap = new HashMap<>();
        for (TreeEntity entity : entities) {
            TreeDTO dto = dtoMap.computeIfAbsent(entity.id, id -> new TreeDTO());
            dto.setName(entity.name);
            dto.setId(entity.id);
            if (entity.parentId == null) {
                rootNodes.add(dto);
                continue;
            }
            TreeDTO parentDto = dtoMap.computeIfAbsent(entity.parentId, id -> new TreeDTO());
            parentDto.children.add(dto);
        }
        return rootNodes;
    }

    public static Collection<TreeDTO> convertWithNoMemory(Collection<TreeEntity> entities) {
        Collection<TreeDTO> rootNodes = new ArrayList<>();
        int totalCount = entities.size();
        int convertedCount = 0;
        while (convertedCount < totalCount) {
            int current = convertedCount;
            for (TreeEntity entity : entities) {
                if (entity.parentId == null) {
                    rootNodes.add(new TreeDTO(entity.id, entity.name));
                    convertedCount++;
                    continue;
                }
                boolean inserted = tryToInsert(entity.id, entity.parentId, entity.name, rootNodes);
                if (inserted) {
                    convertedCount++;
                }
            }
            if (current == convertedCount) {
                throw new IllegalArgumentException("One or more nodes have not existing parend id");
            }
        }
        return rootNodes;
    }

    private static boolean tryToInsert(Integer id, Integer parentId, String name, Collection<TreeDTO> rootNodes) {
        for (TreeDTO rootNode : rootNodes) {
            if (rootNode.id.equals(parentId)) {
                rootNode.getChildren().add(new TreeDTO(id, name));
                return true;
            }
            boolean insertedBelow = tryToInsert(id, parentId, name, rootNode.getChildren());
            if (insertedBelow) {
                return true;
            }
        }
        return false;
    }

    /*
    This method must only be used only if the entities has the special order:
        1. Nodes must have "bypass in breadth" order
        2. Tree must be balanced
        3. For each node height of any it's subtree must not be less than height of any subtree from the right from it
        Example:
             _______1_______
             |             |
          ---2             3
         |
         4
        Valid order:   1 2 3 4
        Invalid order: 1 3 2 4
        Time complexity: O(n)
     */
    public static Collection<TreeDTO> convertOrdered(Collection<TreeEntity> entities) {
        List<TreeDTO> rootNodes = new ArrayList<>();
        int parentIdx = 0;
        List<TreeDTO> currLevel = new ArrayList<>();
        List<TreeDTO> parentLevel = new ArrayList<>();
        final String errorMessage = "Tree nodes are not placed in the proper way";
        for (TreeEntity entity : entities) {
            var entityDto = new TreeDTO(entity.id, entity.name);
            if (entity.parentId == null) {
                rootNodes.add(entityDto);
                parentLevel = List.of(entityDto); // only root in the parent level
                currLevel = new ArrayList<>();
                parentIdx = 0;
                continue;
            }
            if (parentLevel.isEmpty()) {
                throw new IllegalArgumentException(errorMessage);
            }
            var currParent = parentLevel.get(parentIdx);
            if (entity.parentId.equals(currParent.id)) {
                // current parent matches the node parent, just add the entity to childs
                currParent.getChildren().add(entityDto);
                currLevel.add(entityDto);
                continue;
            }
            // current parent not matches the node parent, we should update it
            if (parentIdx < parentLevel.size() - 1) {
                parentIdx++;
            } else {
                parentLevel = currLevel;
                currLevel = new ArrayList<>();
                parentIdx = 0;
            }
            currParent = parentLevel.get(parentIdx);
            if (!entity.parentId.equals(currParent.id)) {
                throw new IllegalArgumentException(errorMessage);
            }
            currParent.getChildren().add(entityDto);
            currLevel.add(entityDto);
        }
        return rootNodes;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TreeEntity {
        private Integer id;
        private String name;
        private Integer parentId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TreeDTO {
        private Integer id;
        private String name;
        private final List<TreeDTO> children = new ArrayList<>();

        public TreeDTO(Integer id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
