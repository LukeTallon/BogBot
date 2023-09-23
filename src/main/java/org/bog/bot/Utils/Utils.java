package org.bog.bot.Utils;

public class Utils {

    //PostgreSQL seems to not like hyphens
    public static String removeHyphensFromTableName(String tableName) {
        StringBuilder sb = new StringBuilder(tableName);
        int index = sb.indexOf("-");
        while (index != -1) {
            sb.replace(index, index + 1, ""); // Remove the hyphen
            index = sb.indexOf("-", index + 1); // Find the next hyphen
        }
        String modifiedTableName = sb.toString();
        return modifiedTableName;
    }
}
