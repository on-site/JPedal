/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/java-pdf-library-support/
 *
 * (C) Copyright 1997-2013, IDRsolutions and Contributors.
 *
 * 	This file is part of JPedal
 *
     This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


 *
 * ---------------
 * DefaultIO.java
 * ---------------
 */
package org.jpedal.render.output.io;

import org.jpedal.io.JAIHelper;
import org.jpedal.io.ObjectStore;
import org.jpedal.utils.LogWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DefaultIO implements CustomIO {

    private BufferedWriter output =null;

    Map imagesWritten=new HashMap(); //@here

    public void writeFont(String path, byte[] rawFontData) {

        try {

            BufferedOutputStream fontOutput = new BufferedOutputStream(new FileOutputStream(path));
            fontOutput.write(rawFontData);
            fontOutput.flush();
            fontOutput.close();

        } catch (Exception e) {
            //tell user and log
            if(LogWriter.isOutput())
                LogWriter.writeLog("Exception: "+e.getMessage());
        }
    }

    public void writeJS(String rootDir, InputStream url) throws IOException {

        //make sure js Dir exists
        String cssPath = rootDir  + "/js";
        File cssDir = new File(cssPath);
        if (!cssDir.exists()) {
            cssDir.mkdirs();
        }

        BufferedInputStream stylesheet = new BufferedInputStream(url);

        BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(rootDir+"/js/aform.js"));
        ObjectStore.copy(stylesheet, bos);
        bos.flush();
        bos.close();

        stylesheet.close();
    }

    public void writeCSS(String rootDir, String fileName, StringBuilder css) {

        //make sure css Dir exists
        String cssPath = rootDir + fileName + '/';
        File cssDir = new File(cssPath);
        if (!cssDir.exists()) {
            cssDir.mkdirs();
        }

        try {
            //PrintWriter CSSOutput = new PrintWriter(new FileOutputStream(cssPath + "styles.css"));
            BufferedOutputStream CSSOutput = new BufferedOutputStream(new FileOutputStream(cssPath + "styles.css"));

            //css header

            CSSOutput.write(css.toString().getBytes());

            CSSOutput.flush();
            CSSOutput.close();

        } catch (Exception e) {
            //tell user and log
            if(LogWriter.isOutput())
                LogWriter.writeLog("Exception: "+e.getMessage());
        }
    }

    public boolean isOutputOpen() {
        return output!=null;
    }

    public void setupOutput(String path, boolean append, String encodingUsed) throws FileNotFoundException, UnsupportedEncodingException {

        output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path,append), encodingUsed));

    }

    public void flush() {

        try{
            output.flush();
            output.close();

            imagesWritten.clear(); //@here

            output =null;
        }catch(Exception e){
            //tell user and log
            if(LogWriter.isOutput())
                LogWriter.writeLog("Exception: "+e.getMessage());
        }
    }

    public void writeString(String str) {

        try {
            output.write(str);
            output.write('\n');
            output.flush();
        } catch (Exception e) {
            //tell user and log
            if(LogWriter.isOutput())
                LogWriter.writeLog("Exception: "+e.getMessage());
        }
    }

    //@here - lots in routine
    public String writeImage(String rootDir,String path, BufferedImage image) {

        String file=path+getImageTypeUsed();
        String fullPath=rootDir+file;

        /**
         * reject repeat images (assume identical name is same)
         * root will include pageNumber as X1 on page 1 and 2 usually different
         */
       // if(!imagesWritten.containsKey(fullPath)){

            //imagesWritten.put(fullPath,"x");

            try{
                if(!JAIHelper.isJAIused()){
                    ImageIO.write(image, "PNG", new File(fullPath));
                } else {
                    JAIHelper.confirmJAIOnClasspath();
                    BufferedOutputStream bos= new BufferedOutputStream(new FileOutputStream(new File(fullPath)));
                    com.sun.media.jai.codec.ImageEncoder encoder = com.sun.media.jai.codec.ImageCodec.createImageEncoder("PNG", bos, null);
                    encoder.encode(image);
                    bos.close();
                }
            }catch(Exception e){
                //tell user and log
                if(LogWriter.isOutput())
                    LogWriter.writeLog("Exception: "+e.getMessage());
            }

        //}

        return file;
    }

    public String getImageTypeUsed(){
        return ".png";
    }
}
