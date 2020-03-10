package life.good.goodlife.model.buttons;

public class Buttons {
    private static String[] newsButton;
    private static String[] mainButton;

    /**
     * 1 - Next five news
     * 2 - General
     * 3 - Music
     * 4 - Entertainment
     * 5 - Health
     * 6 - Science
     * 7 - Technology
     * 8 - Sport
     * 9 - Business
     * @return button
     */
    public static String[] getNewsButton() {
        if (newsButton == null) {
            newsButton = new String[]{"Следущие 5️⃣ новостей 📰", "Главные", "Музыка \uD83C\uDFB6",
                    "Развлечение \uD83D\uDD79", "Здоровье \uD83C\uDFE5", "Наука \uD83E\uDDEC",
                    "Технологии", "Спорт \uD83C\uDFC5", "Бизнес"};
        }
        return newsButton;
    }

    public static String[] getMainButton() {
        if (mainButton == null) {
            mainButton = new String[]{"Главное меню"};
        }
        return mainButton;
    }
}
