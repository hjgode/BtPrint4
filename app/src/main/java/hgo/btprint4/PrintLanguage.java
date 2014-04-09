package hgo.btprint4;

import android.app.*;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.*;
import org.xmlpull.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by hgode on 07.04.2014.
 */

public class PrintLanguage{
    private static String TAG="PrintLanguage";

    public static ePrintLanguages getLanguage(String sFileName){
        ePrintLanguages printLanguage=ePrintLanguages.NAN;
        String sShortName;
        String sFile=sFileName;
        //the first letters describe the print language
        if(sFile.startsWith("csim")){
            printLanguage=ePrintLanguages.CSM;
            sFile=sFileName.substring(4);
            sShortName="CSIM";
        }
        else if(sFile.startsWith("escp")) {
            printLanguage = ePrintLanguages.ESC;
            sFile = sFileName.substring(4);
            sShortName="ESCP";
        }
        else if(sFile.startsWith("fp")){
            printLanguage=ePrintLanguages.FPL;
            sFile=sFileName.substring(2);
            sShortName="FP";
        }
        else if(sFile.startsWith("ipl")) {
            printLanguage = ePrintLanguages.ESC;
            sFile = sFileName.substring(3);
            sShortName="IPL";
        }
        else if(sFile.startsWith("xsim")) {
            printLanguage = ePrintLanguages.XSM;
            sFile = sFileName.substring(4);
            sShortName="XSIM";
        }
        else{
            printLanguage=ePrintLanguages.NAN;
            sShortName="unknown";
            Log.e(TAG,"unknown print language: '"+sFile+"'");
        }
        return printLanguage;
    }//PrintLanguage constructor

    public static Integer getPrintWidth(String sFileName){
        Integer iPrintWidth=2;
        char[] chars = sFileName.toCharArray();
        int code=2;
        try{
        for(int i=0; i<sFileName.length(); i++){
            //Integer code = sFileName.substring(i,1).codePointAt(0);
            code=chars[i];
            if(code>=48 && code<=57){   //numbers are from 48 to 57
                try {
                    iPrintWidth = code-48;
                }catch(Exception e){
                    Log.e(TAG, "exception converting '"+ sFileName.substring(i,1) +"' to Integer, using 2\"");
                    iPrintWidth=2;
                }
            }
        }}catch (Exception e){
            Log.e(TAG, "Exception in getPrintWidth: "+e.getMessage());
        }
        return  iPrintWidth;
    }

    public enum ePrintLanguages{
        NAN,
        IPL,
        ZPL,
        DPL,
        ESC,    //escp
        XSM,    //xsim
        CSM,    //CSIM
        FPL     //fingerprint
    }
}
