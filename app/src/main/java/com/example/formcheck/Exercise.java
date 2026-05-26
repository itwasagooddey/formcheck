package com.example.formcheck;

public class Exercise {

    public static final String SQUATS = "Squats";
    public static final String PUSHUPS = "Push-ups";
    public static final String PULLUPS = "Pull-ups";

    public static final int SQUATS_GOOD_MAX = 90;
    public static final int SQUATS_LOW_MAX = 110;

    public static final int PUSHUPS_GOOD_MAX = 90;
    public static final int PUSHUPS_LOW_MAX = 110;

    public static final int PULLUPS_GOOD_MAX = 90;
    public static final int PULLUPS_LOW_MAX = 120;

    public final String name;
    public final String description;
    public final String whatIsGood;
    public final String whatIsBad;
    public final String instruction;
    public final int goodAngleMax;
    public final int lowAngleMax;

    public Exercise(String name) {
        this.name = name;
        switch (name) {
            case SQUATS:
                description = "Приседания со своим весом";
                whatIsGood = "Хорошо: колени сгибаются до 90° или ниже, спина прямая, пятки на полу";
                whatIsBad = "Плохо: угол колена выше 110°, колени заваливаются внутрь, пятки отрываются";
                instruction = "Встаньте в 1–1.5 м от камеры боком. Камера на высоте бедра. Всё тело в кадре.";
                goodAngleMax = SQUATS_GOOD_MAX;
                lowAngleMax = SQUATS_LOW_MAX;
                break;
            case PUSHUPS:
                description = "Отжимания от пола";
                whatIsGood = "Хорошо: локти сгибаются до 90°, тело — прямая линия, грудь касается пола";
                whatIsBad = "Плохо: угол локтя выше 110°, провисание или горб в пояснице";
                instruction = "Лягте боком к камере. Камера на высоте плеча на расстоянии 1.5 м.";
                goodAngleMax = PUSHUPS_GOOD_MAX;
                lowAngleMax = PUSHUPS_LOW_MAX;
                break;
            case PULLUPS:
                description = "Подтягивания на турнике";
                whatIsGood = "Хорошо: подбородок выше штанги, локти до 90° в верхней точке";
                whatIsBad = "Плохо: угол локтя выше 120°, раскачка корпуса, неполное выпрямление рук внизу";
                instruction = "Снимайте сбоку на расстоянии 1.5–2 м. Турник должен быть полностью в кадре.";
                goodAngleMax = PULLUPS_GOOD_MAX;
                lowAngleMax = PULLUPS_LOW_MAX;
                break;
            default:
                description = "";
                whatIsGood = "";
                whatIsBad = "";
                instruction = "";
                goodAngleMax = 90;
                lowAngleMax = 120;
        }
    }
}
