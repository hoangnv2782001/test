/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller.client;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author Administrator
 */
public class FileDownload {

    private String filename;
    private byte[] file;

    public FileDownload(String filename, byte[] file) {
        this.filename = filename;
        this.file = Arrays.copyOf(file, file.length);
    }
    public void execute() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(filename));
        int k = fileChooser.showSaveDialog(null);
        if (k == JFileChooser.APPROVE_OPTION) {

            // lưu file vào vị tríđã chọn 
            File saveFile = fileChooser.getSelectedFile();
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(saveFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

//            // Hiển thị JOptionPane cho người dùng có muốn mở file vừa tải về không
//            int nextAction = JOptionPane.showConfirmDialog(null, "Lưu file?", "Successful", JOptionPane.YES_NO_OPTION);
//            if (nextAction == JOptionPane.YES_OPTION) {
//                try {
//                    Desktop.getDesktop().open(saveFile);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }

            if (bos != null) {
                try {
                    bos.write(this.file);
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
