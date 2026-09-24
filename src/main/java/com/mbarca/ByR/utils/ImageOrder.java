package com.mbarca.ByR.utils;
import java.util.*;
public final class ImageOrder {
    private ImageOrder() {}
    public static List<Integer> normalize(List<Integer> requested, int imageCount) {
        Set<Integer> order = new LinkedHashSet<>();
        if (requested != null) for (Integer index : requested)
            if (index != null && index >= 0 && index < imageCount) order.add(index);
        for (int index = 0; index < imageCount; index++) order.add(index);
        return new ArrayList<>(order);
    }
    public static List<Integer> afterRemoval(List<Integer> requested, int imageCount, int removed) {
        return normalize(requested, imageCount).stream().filter(i -> i != removed)
                .map(i -> i > removed ? i - 1 : i).toList();
    }
}
