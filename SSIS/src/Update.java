import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

public class Update implements ActionListener {
    private final DefaultTableModel model;
    private final JTable table;
    private final List<JComponent> fields;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final CSVHandler csvHandler;
    private final String f;

    public Update(DefaultTableModel model, JTable table, List<JComponent> fields, TableRowSorter<DefaultTableModel> sorter, CSVHandler csvHandler, String f){
        this.model = model;
        this.table = table;
        this.fields = fields;
        this.sorter = sorter;
        this.csvHandler = csvHandler;
        this.f = f;
    }
    public boolean Compare(String[] data, int index){
        List<String[]> csvData = csvHandler.readCSV();
        System.out.println(Arrays.toString(csvData.get(index)));
        csvData.remove(index);
        for (String[] cData : csvData){
            if (cData[0].equals(data[0])){
                return true;
            }
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String[] data = new String[fields.size()];
        String[] header = csvHandler.readHeaders();

        boolean valid = true;
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i) instanceof JFormattedTextField) {
                data[i] = ((JFormattedTextField) fields.get(i)).getText().trim();
                if (Fields.validateID(data[i])) {
                    JOptionPane.showMessageDialog(null, model.getColumnName(0) + " has to above 2000 and below Current Year", null, JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if (fields.get(i) instanceof JTextField) {
                data[i] = ((JTextField) fields.get(i)).getText().trim();
            } else if (fields.get(i) instanceof JComboBox<?>) {
                Object selectedItem = ((JComboBox<?>) fields.get(i)).getSelectedItem();
                String name = header[header.length - 1];
                if (selectedItem == null || selectedItem.equals("Add New") || selectedItem.equals("---------")) {
                    JOptionPane.showMessageDialog(null, "No " + name + " selected!", "Invalid " + name, JOptionPane.ERROR_MESSAGE);
                    valid = false;
                    break;
                }
                Object lastselectedItem = ((JComboBox<?>) fields.getLast()).getSelectedItem();

                if (lastselectedItem != null && Fields.existinCSV(lastselectedItem.toString(), f)){
                    JOptionPane.showMessageDialog(null, "No such data in the " + name + " Column.", "Invalid Data", JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    data[i] = selectedItem.toString();
                }
            }
        }


        if (!valid) return;
        for (String val : data){
            if (val.isEmpty()){
                JOptionPane.showMessageDialog(null,
                        "Data incomplete! Please make sure to put data on all field.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (table.getSelectedRow() == -1){
            JOptionPane.showMessageDialog(null, "No row selected! If you selected one, make sure it shows up on the field above by pressing again.", "Row Selection Error", JOptionPane.ERROR_MESSAGE);
        }else {
                int rowIndex = sorter.convertRowIndexToModel(table.getSelectedRow());
                if (Compare(data, rowIndex)){
                    JOptionPane.showMessageDialog(null, model.getColumnName(0) + " already exist", null, JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String delVal = (String) model.getValueAt(rowIndex, 0);
                csvHandler.updateCSV(rowIndex, data);
                GUI.loadData(model, csvHandler);
                GUI.updateReferences(delVal, f);
                Fields.clearFields(fields);
            JOptionPane.showMessageDialog(null, "Updated Successfully", "Success", JOptionPane.INFORMATION_MESSAGE);

        }

    }
}
