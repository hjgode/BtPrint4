package hgo.btprint4;

import android.app.Activity;
import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by hgode on 08.04.2014.
 */
public class PrintFileDetails {
    public String shortname;
    public String description;
    public String help;
    public String filename;
    public PrintLanguage.ePrintLanguages printLanguage;
    public Integer printerWidth=2;

    public PrintFileDetails(){
        this.printerWidth=2;
        this.printLanguage= PrintLanguage.ePrintLanguages.NAN;
        this.description="undefined";
        this.filename="no file";
        this.help="empty entry";
        this.shortname="do not use";
    }
    @Override
    public String toString(){
        String s="unknown";
        s=String.format("description: %s\nprint language: %s\nprint width: %s\nfile name: %s",
                this.description,
                this.printLanguage,
                this.printerWidth,
                this.filename);
        return s;
    }
    public String getName(){
        return this.filename;
    }
}