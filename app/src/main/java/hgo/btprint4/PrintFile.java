package hgo.btprint4;

import android.app.Activity;
import android.util.Log;

import java.io.IOException;

/**
 * Created by hgode on 07.04.2014.
 */
public class PrintFile {
    String sFileName;
    ePrintLanguages pLang=ePrintLanguages.NAN;

    public ePrintLanguages _getPrintLanguage(){
        return this.pLang;
    }

    public PrintFile(String sFile){
        sFileName=sFile;
        PrintLanguage pL=new PrintLanguage(sFileName);
        pLang=pL.printLanguage;
    }

    public class PrintLanguage{
        String sShortName;// ie IPL or ESC or DPL
        Integer  fPrintWidth;
        ePrintLanguages printLanguage;

        private final String TAG="PrintLanguage";

        public PrintLanguage(String sFile){
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
            //the next char is the printwidth
            String w=sFile.substring(0,1);
            try {
                fPrintWidth = Integer.getInteger(w);
            }catch(Exception e){
                Log.e(TAG, "exception converting '"+w+"' to Integer, using 2\"");
                fPrintWidth=2;
            }
        }//PrintLanguage constructor
    }//Printlanguage class

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
