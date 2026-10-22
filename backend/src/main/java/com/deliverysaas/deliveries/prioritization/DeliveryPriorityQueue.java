package com.deliverysaas.deliveries.prioritization;

import java.util.ArrayList;
import java.util.List;

public class DeliveryPriorityQueue {

    private final List<PrioritizedDelivery> heap = new ArrayList<>();

    public void add(PrioritizedDelivery item) {
        heap.add(item);
        siftUp(heap.size() - 1);
    }

    public PrioritizedDelivery poll() {
        if (heap.isEmpty()) {
            return null;
        }
        PrioritizedDelivery top = heap.get(0);
        PrioritizedDelivery last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            siftDown(0);
        }
        return top;
    }

    public PrioritizedDelivery peek() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public int size() {
        return heap.size();
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (heap.get(index).score() <= heap.get(parent).score()) {
                break;
            }
            swap(index, parent);
            index = parent;
        }
    }

    private void siftDown(int index) {
        int size = heap.size();
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int largest = index;

            if (left < size && heap.get(left).score() > heap.get(largest).score()) {
                largest = left;
            }
            if (right < size && heap.get(right).score() > heap.get(largest).score()) {
                largest = right;
            }
            if (largest == index) {
                break;
            }
            swap(index, largest);
            index = largest;
        }
    }

    private void swap(int i, int j) {
        PrioritizedDelivery temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}