import org.w3c.dom.events.DocumentEvent;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;
public class CSVfileloader {
    public static void main(String[] args) {
        new CSVfileloader();
    }

    private JFrame guiMainForm;
    JTextField csvUrltextfield = new JTextField("no string yet");
    JPanel compnentPanel = null;
    JPanel perviewpnl = null;
    JPanel mainpanel = null;
    JTable dataPerviewtable = null;
    JScrollPane js;
    public CSVfileloader() {
        mainpanel = new JPanel();
        guiMainForm = new JFrame();
        guiMainForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiMainForm.setName("CSV loader");
        guiMainForm.setSize(1000, 900);
        guiMainForm.setLocationRelativeTo(null);
        //main window
        JButton addCSVUrlbtn = new JButton("ADD");
        addCSVUrlbtn.setSize(100, 70);
        JButton openCSVUrlbtn = new JButton("OPEN");
        addCSVUrlbtn.setSize(100, 70);
        addCSVUrlbtn.addActionListener(AddUrlfile);
        openCSVUrlbtn.addActionListener(openCSVFile);
        compnentPanel = new JPanel();
        compnentPanel.add(addCSVUrlbtn);
        compnentPanel.add(openCSVUrlbtn);
        dataPerviewtable=new JTable();

        perviewpnl = new JPanel();
        perviewpnl.setLayout(new BoxLayout(perviewpnl, BoxLayout.Y_AXIS));
        perviewpnl.add(dataPerviewtable);
        //you can not add null object to your panel to show it
        mainpanel.add(compnentPanel);
        mainpanel.add(perviewpnl);
        mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
        guiMainForm.add(mainpanel);
        guiMainForm.setResizable(false);
        guiMainForm.setVisible(true);
    }

    ActionListener AddUrlfile = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String csvUrl = JOptionPane.showInputDialog(csvUrltextfield, "enter the url please");
//            while (!Test.isUrlValid(csvUrl)){
//                JOptionPane.showMessageDialog(csvUrltextfield,"please enter a valid url");
//                csvUrl=JOptionPane.showInputDialog(csvUrltextfield ,"enter the url please");
            URL website = null;
            try {
                website = new URL(csvUrl);
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
            ReadableByteChannel rbc = null;
            try {
                rbc = Channels.newChannel(website.openStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream("information.csv");
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            try {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    };
    ActionListener openCSVFile = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            File selectedFile = null;
            //
            JFileChooser chooseCSVfile = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            int return_value = chooseCSVfile.showOpenDialog(null);
            if (return_value == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooseCSVfile.getSelectedFile();
            }
            String filepath = selectedFile.getAbsolutePath();
//          code to browse and choose file and then return its absolute path to open it in jTable later
//            String[][] data = {
//                    {"Kundan Kumar Jha", "4031", "CSE"},
//                    {"Anand Jha", "6014", "IT"}
//            };
//            String[] columnNames = { "Name", "Roll Number", "Department" };
            ArrayList<String[]>data=new ArrayList<String[]>();
            int i=0;
            try(Stream<String>datastream=Files.lines(Paths.get(filepath))){
                datastream.forEach(names->{
                    data.add(names.split(","));
                });

            }catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
            String[] columnNames=data.get(0);
            //String[][]data2= (String[][]) data.toArray();
            String[][] data2=new String[data.size()][];
            for(int c=0;c<data.size();c++){
                data2[c]=data.get(c);
            }
            TableModel dataset=new DefaultTableModel(data2,columnNames);
//            JScrollPane js=new JScrollPane(dataPerviewtable);
//            perviewpnl.add(js);
            //js.setVisible(true);
              dataPerviewtable.setModel(dataset);
            js= new JScrollPane(dataPerviewtable);
            // dataPerviewtable.add(js);
            js.setVisible(true);
            perviewpnl.add(js);
            JPanel filterarea=new JPanel();

            JTextField filterField=RowFilterUtil.createRowFilter(dataPerviewtable);
            filterarea.add(filterField);
            perviewpnl.add(filterarea);

        }



    };
}
class RowFilterUtil {
    public static JTextField createRowFilter(JTable table) {
        RowSorter<? extends TableModel> rs = table.getRowSorter();
        if (rs == null) {
            table.setAutoCreateRowSorter(true);
            rs = table.getRowSorter();
        }

        TableRowSorter<? extends TableModel> rowSorter =
                (rs instanceof TableRowSorter) ? (TableRowSorter<? extends TableModel>) rs : null;

        if (rowSorter == null) {
            throw new RuntimeException("Cannot find appropriate rowSorter: " + rs);
        }

        final JTextField tf = new JTextField(15);
        tf.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                update((javax.swing.event.DocumentEvent) e);
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                update((javax.swing.event.DocumentEvent) e);
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                update((javax.swing.event.DocumentEvent) e);
            }

            private void update(javax.swing.event.DocumentEvent e) {
                String text = tf.getText();
                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        return tf;
    }
}
