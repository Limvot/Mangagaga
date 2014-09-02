package io.githup.limvot.mangaapp;

/**
 * Created by marcus on 9/2/14.
 */
public class SettingsManager {
    private static SettingsManager settings;
    private static int historySize;

    public SettingsManager()
    {
        historySize = 10;
    }

    public static SettingsManager getSettingsManager()
    {
        if(settings == null)
            settings = new SettingsManager();
        return settings;
    }

    public void setHistorySize(int size)
    {
        historySize = size;
    }

    public int getHistorySize()
    {
        return historySize;
    }
}
