/*
 *  Copyright (C) 2015 MummyDing
 *
 *  This file is part of Leisure( <https://github.com/MummyDing/Leisure> )
 *
 *  Leisure is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *                             ｀
 *  Leisure is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Leisure.  If not, see <http://www.gnu.org/licenses/>.
 */

package cn.edu.ustc.igank.support;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;


import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cn.edu.ustc.igank.IGankApplication;
import cn.edu.ustc.igank.R;
import cn.edu.ustc.igank.database.DatabaseHelper;
import cn.edu.ustc.igank.database.table.AndroidTable;
import cn.edu.ustc.igank.database.table.ExtendTable;
import cn.edu.ustc.igank.database.table.GirlTable;
import cn.edu.ustc.igank.database.table.IOSTable;
import cn.edu.ustc.igank.database.table.WebDesignTable;

public class Utils {

    private static boolean DEBUG = true;
    private static Context mContext = IGankApplication.AppContext;
    public static InputStream readFileFromRaw(int fileID){
       return mContext.getResources()
               .openRawResource(fileID);
    }

    public static Document getDocmentByIS(InputStream is){
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document doc =null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
             doc = builder.parse(is);
             is.close();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc ;
    }

    // convert InputStream to String
    public static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }
    public static String RegexFind(String regex,String string,int start,int end){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        String res = string;
        while (matcher.find()){
            res = matcher.group();
        }
        return res.substring(start, res.length() - end);
    }
    public static String RegexFind(String regex,String string){
        return RegexFind(regex, string, 1, 1);
    }
    public static String RegexReplace(String regex,String string,String replace){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        return matcher.replaceAll(replace);
    }
    public static boolean hasString(String str){
        if(str == null || str.equals("")) return false;
        return true;
    }
    public static void showToast(String text){
        Toast.makeText(mContext,text,Toast.LENGTH_SHORT).show();
    }
    public static void DLog(String text){
        if(DEBUG) {
            Log.d(mContext.getString(R.string.text_debug_data), text);
        }
    }
    public static String getImageHtml(){
        return "<head><style>img{max-width: 320px !important;}</style></head>";
    }

    public static int getCurrentLanguage() {
        int lang = Settings.getInstance().getInt(Settings.LANGUAGE, -1);
        if (lang == -1) {
            String language = Locale.getDefault().getLanguage();
            String country = Locale.getDefault().getCountry();

            if (language.equalsIgnoreCase("zh")) {
                lang = 1;
            } else {
                lang = 0;
            }
        }
        return lang;
    }
    public static void clearCache(){


        WebView wb = new WebView(mContext);
        wb.clearCache(true);

        DatabaseHelper mHelper = DatabaseHelper.instance(mContext);
        SQLiteDatabase db = mHelper.getWritableDatabase();

        db.execSQL(mHelper.DELETE_TABLE_DATA + GirlTable.NAME);
        db.execSQL(mHelper.DELETE_TABLE_DATA + AndroidTable.NAME);
        db.execSQL(mHelper.DELETE_TABLE_DATA + IOSTable.NAME);
        db.execSQL(mHelper.DELETE_TABLE_DATA + WebDesignTable.NAME);
        db.execSQL(mHelper.DELETE_TABLE_DATA + ExtendTable.NAME);
    }

    // Must be called before setContentView()
    public static void changeLanguage(Context context, int lang) {
        String language = null;
        String country = null;

        switch (lang) {
            case 1:
                language = "zh";
                country = "CN";
                break;
            default:
                language = "en";
                country = "US";
                break;
        }

        Locale locale = new Locale(language, country);
        Configuration conf = context.getResources().getConfiguration();
        conf.locale = locale;
        context.getApplicationContext().getResources().updateConfiguration(conf, context.getResources().getDisplayMetrics());
    }

    public static String getVersion() {
        try {
            PackageManager manager = mContext.getPackageManager();
            PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
            String version = info.versionName;
            return  version;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void copyToClipboard(View view,String info) {
        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(mContext.CLIPBOARD_SERVICE);
        ClipData cd = ClipData.newPlainText("msg", info);
        cm.setPrimaryClip(cd);
        Snackbar.make(view, R.string.notify_info_copied, Snackbar.LENGTH_SHORT).show();
    }



    /**
     * 获得当前系统的亮度值： 0~255
     */
    /** 可调节的最大亮度值 */
    public static final int MAX_BRIGHTNESS = 255;
    public static int getSysScreenBrightness() {
        int screenBrightness = MAX_BRIGHTNESS;
        try {
            screenBrightness = android.provider.Settings.System.getInt(mContext.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            Utils.DLog("获得当前系统的亮度值失败：");
        }
        return screenBrightness;
    }

    /**
     * 设置当前系统的亮度值:0~255
     */
    public static void setSysScreenBrightness(int brightness) {
        try {
            ContentResolver resolver = mContext.getContentResolver();
            Uri uri = android.provider.Settings.System.getUriFor(android.provider.Settings.System.SCREEN_BRIGHTNESS);
            android.provider.Settings.System.putInt(resolver, android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);
            resolver.notifyChange(uri, null); // 实时通知改变
        } catch (Exception e) {
            Utils.DLog("设置当前系统的亮度值失败："+e);
        }
    }


}
