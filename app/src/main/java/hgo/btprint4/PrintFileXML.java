package hgo.btprint4;

import android.app.Activity;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by hgode on 08.04.2014.
 */
public class PrintFileXML {
    public ArrayList<PrintFileDetails> printFileDetails;
    final String TAG="PrintFileXML";

    public PrintFileXML(InputStream in_s) {
        XmlPullParserFactory pullParserFactory;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();

            //InputStream in_s = activity.getApplicationContext().getAssets().open(fileXML);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);

            printFileDetails = parseXML(parser);
        } catch (XmlPullParserException e) {
            Log.e(TAG, "XmlPullParserException: " + e.getMessage());
            //e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "IOException: " + e.getMessage());
            //e.printStackTrace();
        }
        finally {
        }
    }

    private ArrayList<PrintFileDetails> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<PrintFileDetails> printfiles = null;
        printfiles = new ArrayList<PrintFileDetails>();

        int eventType = parser.getEventType();
        PrintFileDetails currentPrintFile = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
//                    printfiles = new ArrayList();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    Log.i("XmlParser", "name='"+name+"'");
                    if (name.equals("fileentry")) {
                        currentPrintFile = new PrintFileDetails();
                    } else if (currentPrintFile != null) {
                        if (name.equals("shortname")) {
                            currentPrintFile.shortname = parser.nextText();
                        } else if (name.equals("decription")) {
                            currentPrintFile.description = parser.nextText();
                        } else if (name.equals("help")) {
                            currentPrintFile.help = parser.nextText();
                        } else if (name.equals("filename")) {
                            currentPrintFile.filename = parser.nextText();
                            currentPrintFile.printLanguage=PrintLanguage.getLanguage(currentPrintFile.filename);
                            currentPrintFile.printerWidth=PrintLanguage.getPrintWidth(currentPrintFile.filename);
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("fileentry") && currentPrintFile != null) {
                        printfiles.add(currentPrintFile);
                    }
            }
            eventType = parser.next();
        }
        return printfiles;
    }

    public PrintFileDetails getPrintFileDetails(String sFileName){
        PrintFileDetails printFileDetails1=new PrintFileDetails();
        for(PrintFileDetails pd : printFileDetails) {
            if (pd.filename.equals(sFileName)) {
                printFileDetails1 = pd;
                return printFileDetails1;
            }
        }
        return printFileDetails1;
    }
}
