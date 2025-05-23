

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;


public class GUI extends JFrame {

    static String[] file = {"SSIS/csv/Student.csv"
            , "SSIS/csv/Program.csv"
            , "SSIS/csv/College.csv"};

    JTabbedPane tab = new JTabbedPane();
    String[] column;
    List<DefaultTableModel> models = new ArrayList<>();
    List<CSVHandler> csvHandlers = new ArrayList<>();
    List<JTable> tables = new ArrayList<>();
    List<List<JComponent>> fields = new ArrayList<>();

    public GUI() {
        initComponents();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Student Information System");
        setSize(840, 540);
        setLocationRelativeTo(null);

        for (String s : file) {
            tab.addTab(dbName(s), PanelLayout(s));
        }

        tab.addChangeListener(_ -> {
            int index = tab.getSelectedIndex();
            if (index != -1) {
                loadData(models.get(index), csvHandlers.get(index));

                List<JComponent> field = fields.get(index);
                    if (field.getLast() instanceof JComboBox) {
                        JComboBox<String> fk = (JComboBox<String>) field.getLast();
                        Fields fHelper = new Fields(file);
                        fHelper.refreshfk(fk, file[index]); // proper call
                    }

            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(tab)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(tab, GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                                .addContainerGap())
        );

    }

    public String dbName(String file){
        return file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf("."));
    }

public JPanel PanelLayout(String f) {
        JPanel MainPanel = new JPanel();
        MainPanel.setPreferredSize(new Dimension(740, 440));
        GroupLayout panelLayout = new GroupLayout(MainPanel);
        MainPanel.setLayout(panelLayout);

        CSVHandler csvHandler = new CSVHandler(f);
        csvHandlers.add(csvHandler);



        try {
            column = csvHandler.getHeaders();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error reading headers: " + e.getMessage());
            column = new String[]{};
        }
        DefaultTableModel model = new DefaultTableModel(column, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        models.add(model);

        JTable table = new JTable(model);
        tables.add(table);
        Fields infield = new Fields(file);
        JScrollPane sp = new JScrollPane(table);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        List<JComponent> field = infield.createFields(model, f);
        fields.add(field);

        Edit_Panel edit_panel = new Edit_Panel(sorter, model, field, csvHandler, table);
        Tab_Panel tab_panel = new Tab_Panel(sorter, model, csvHandler);

        JPanel tabPanel = tab_panel.createTabPanel(dbName(f), sp);
        JPanel editPanel = edit_panel.createEditPanelLayout(f);


        table.addMouseListener(TableListener(table, model, field, sorter));

        loadData(model, csvHandler);

        return Layout.MainPanelLayout(MainPanel, tabPanel, editPanel);
    }

    public static void loadData(DefaultTableModel model, CSVHandler csvHandler) {
        model.setRowCount(0);
        try {
            List<String[]> data = csvHandler.readCSV();

            for (String[] row : data) {
                String[] cleanRow = new String[row.length];
                for (int i = 0; i < row.length; i++) {
                    cleanRow[i] = (row[i].equals("null") || row[i].trim().isEmpty()) ? "N/A" : row[i];
                }
                model.addRow(cleanRow);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading data: " + e.getMessage());
        }
    }

    public static void updateReferences(String refval, String f) {
        int index = Arrays.asList(file).indexOf(f) - 1;
        CSVHandler ref;
        if (index > -1 && index < file.length) {
            ref = new CSVHandler(file[index]);
            List<String[]> refCSV = ref.readCSV();
            for (String[] refData : refCSV) {
                if (refData[refData.length - 1].equals(refval)) {
                    refData[refData.length - 1] = null;
                }
            }
            ref.writeCSV(refCSV);
        }
    }
    private MouseAdapter TableListener(JTable table, DefaultTableModel model, List<JComponent> fields, TableRowSorter<DefaultTableModel> sorter) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    return;
                }
                selectedRow = sorter.convertRowIndexToModel(selectedRow);
                for (int i = 0; i < model.getColumnCount(); i++) {
                    if (i < fields.size()) {
                        JComponent field = fields.get(i);
                        Object value = model.getValueAt(selectedRow, i);

                        if (field instanceof JTextField) {
                            ((JTextField) field).setText(value != null ? value.toString() : "");
                        } else if (field instanceof JComboBox) {
                            ((JComboBox<?>) field).setSelectedItem(value);
                        }
                    }
                }
            }

        };
    }
}