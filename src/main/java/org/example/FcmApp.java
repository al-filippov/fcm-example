package org.example;

import java.util.ArrayList;
import java.util.List;

public class FcmApp {
    public static void main(String[] args) {
        final List<Integer> points = new ArrayList<>();
        points.add(0);
        points.add(1);
        points.add(2);
        points.add(10);
        points.add(11);
        points.add(12);
        points.add(20);
        points.add(21);
        points.add(22);
        final FcmInteger fcm = new FcmInteger(points, 3, 1.3, 0.00001, 200);
        System.out.println(fcm.fcm());

        final List<List<Term>> docs = new ArrayList<>();
        docs.add(List.of(new Term("система", 0.8), new Term("информационная", 0.9)));
        docs.add(List.of(new Term("поддержка", 0.1), new Term("информационная", 0.9)));
        docs.add(List.of(new Term("система", 0.8), new Term("водопроводная", 0.1)));
        docs.add(List.of(new Term("новости", 0.5), new Term("ульяновска", 0.6)));
        final FcmTerms fcmTerms = new FcmTerms(docs, 3, 1.3, 0.00001, 200);
        System.out.println(fcmTerms.fcm());
    }
}
