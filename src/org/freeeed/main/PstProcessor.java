package org.freeeed.main;
import java.io.File;
import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class PstProcessor {
    private String pstFilePath;
    private Context context;
    
    public PstProcessor(String pstFilePath, Context context) {
        this.pstFilePath = pstFilePath;
        this.context = context;
    }
    // TODO improve PST file type detection
    public static boolean isPST(String fileName) {
        if ("pst".equalsIgnoreCase(Util.getExtension(fileName))) {
            return true;
        }
        return false;
    }
    public void process() throws IOException, InterruptedException {
        String outputDir = "pst_output";
        LinuxUtil.runLinuxCommand("rm -fr " + outputDir);
        extractEmails(pstFilePath, outputDir);
        collectEmails(outputDir);
    }
    private void collectEmails(String emailDir) throws IOException, InterruptedException {
        if (new File(emailDir).isFile()) {
            EmlFileProcessor fileProcessor = new EmlFileProcessor(emailDir, context);
            fileProcessor.process();
            return;
        } else {
            File files[] = new File(emailDir).listFiles();
            for (File file: files) {
                collectEmails(file.getPath());
            }
        }                
    }
    /**
     * Extract the emails with appropriate options, follow this sample format
     * readpst -e -D -o myoutput zl_bailey-s_000.pst
     */
    private void extractEmails(String pstPath, String outputDir) throws IOException, InterruptedException {
        String error = LinuxUtil.verifyReadpst();
        if (error != null) {
            System.out.println(error);
            throw new InterruptedException("Not all pre-requisites installed");
        }
        new File(outputDir).mkdir();
        String command = "readpst -e -D -o " + outputDir + " " + pstPath;
        LinuxUtil.runLinuxCommand(command);
        collectEmails(outputDir);
    }
    
}