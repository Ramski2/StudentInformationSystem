import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Fields {
    private static String[] file;

    public Fields(String[] file) {
        Fields.file = file;
    }


    public List<JComponent> createFields(DefaultTableModel model, String f) {
        List<JComponent> fields = new ArrayList<>();

        JComboBox<String> fk = new JComboBox<>();

        int index = Arrays.asList(file).indexOf(f) + 1;

        if (index == 1) {
            JFormattedTextField field = new JFormattedTextField(format());

            int curryear = Year.now().getValue();
            String idNum = getIncrement(curryear, f);
            field.setText(curryear + idNum);
            fields.add(field);

            for (int i = 0; i < model.getColumnCount() - 4; i++) {
                fields.add(new JTextField());
            }
            String[] gender = {"---------","Male", "Female", "Other", "Rather Not Say"};
            String[] year = {"---------","1", "2", "3", "4"};

            fields.add(new JComboBox<>(gender));
            fields.add(new JComboBox<>(year));

            fields.add(fk);
            fKey(fk, index);
        } else if (index == 2) {
            for (int i = 0; i < model.getColumnCount() - 1; i++) {
                fields.add(new JTextField());
            }
            fields.add(fk);
            fKey(fk, index);

        } else {
            for (int i = 0; i < model.getColumnCount(); i++) {
                fields.add(new JTextField());
            }
        }

        fk.addItemListener(e -> {
            if ("Add New".equals(fk.getSelectedItem())){
                fk.setSelectedIndex(0);
                InputWindow(f, fk);

            }
        });

        return fields;
    }

    public void fKey(JComboBox<String> fk, int index) {
        fk.removeAllItems();
        fk.addItem("---------");
        fk.addItem("Add New");

        if (index < file.length) {
            CSVHandler csvHandler = new CSVHandler(file[index]);
            List<String[]> csvData = csvHandler.readCSV();
            List<String> values = boxValue(csvData);

            for (String value : values) {
                fk.addItem(value);
            }
        }
    }

    public List<String> boxValue(List<String[]> csvData) {
        List<String> values = new ArrayList<>();
        for (String[] row : csvData) {
            if (row.length > 0) {
                values.add(row[0]);
            }
        }
        return values;
    }

    public MaskFormatter format() {
        MaskFormatter format;
        try {
            format = new MaskFormatter("####-####");
            format.setValidCharacters("0123456789");

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return format;
    }

    public static boolean existinCSV(String selectedItem, String f){
        int index = Arrays.asList(file).indexOf(f) + 1;
        CSVHandler newCSV = new CSVHandler(file[index]);
        List<String[]> cData = newCSV.readCSV();
        for (String[] c : cData) {
            System.out.println(Arrays.toString(c));
            System.out.printf("Comparing: '%s' with '%s'%n", selectedItem, c[0]);
            if (selectedItem.equals(c[0])) {
                return false;
            }
        }
        return true;
    }

    public static boolean validateID(String data) {
        String[] id = data.split("-");
        System.out.println(id[1]);

        try {
            int year = Integer.parseInt(id[0]);
            int currentyear = Year.now().getValue();
            if (year < 2000 || year > currentyear) return true;
            Integer.parseInt(id[1]);

            return false;
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clearFields(List<JComponent> fields) {
        for (JComponent tfield : fields) {
            if (tfield instanceof JTextField) {
                ((JTextField) tfield).setText("");
            } else if (tfield instanceof JComboBox<?>) {
                ((JComboBox<?>) tfield).setSelectedItem(-1);
            }
        }
    }

    private String getIncrement(int year, String f) {
        CSVHandler csvHandler = new CSVHandler(f);
        List<String[]> csvData = csvHandler.readCSV();
        int maxNum = 0;

        for (String[] row : csvData) {
            if (row.length > 0) {
                String[] parts = row[0].split("-");
                if (parts.length == 2) {
                    try {
                        int rowYear = Integer.parseInt(parts[0]);
                        int rowNum = Integer.parseInt(parts[1]);

                        if (rowYear == year && rowNum > maxNum) {
                            maxNum = rowNum;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        maxNum++;
        return String.format("%04d", maxNum);
    }
    private boolean isDialogOpen = false;
    private void InputWindow(String f, JComboBox<String> fk) {
        if (isDialogOpen) return;
        isDialogOpen = true;
        int index = Arrays.asList(file).indexOf(f) + 1;
        if (index >= file.length) return;


        CSVHandler csvHandler = new CSVHandler(file[index]);
        String[] data = csvHandler.readHeaders();
        System.out.println(data.length);
        List<JComponent> fields = new ArrayList<>();
        List<JLabel> labels = new ArrayList<>();

        JDialog dialog = new JDialog();
        dialog.setTitle("New Entry");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // For JDialog
        dialog.setResizable(false);
        dialog.setModal(true); // Makes the dialog modal

        for (int i = 0; i < (data.length); i++){
            labels.add(new JLabel(data[i]));
            JComponent field = (f.equals(file[0]) && i == data.length - 1) ? new JComboBox<>() : new JTextField();
            if (field instanceof JComboBox<?>){
                JComboBox<String> cb = (JComboBox<String>) field;
                fKey(cb, index+1);
                cb.removeItem("Add New");
            }
            fields.add(field);
        }

        JPanel inputWin = Layout.InputWinLayout(fields, labels);

        JLabel title = new JLabel("Add New");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton save = new JButton("Save");

        save.addActionListener(e-> {
            new Create(csvHandler, fields ,file[index]).actionPerformed(e);
            isDialogOpen = false;
            refreshfk(fk, f);
            dialog.dispose();
        });

        JPanel contentPane = Layout.AddNewContentPaneLayout(title, inputWin, save);
        dialog.setContentPane(contentPane);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

    }
    public void refreshfk(JComboBox<String> fk, String f) {
        int index = Arrays.asList(file).indexOf(f) + 1;
        if (index < file.length) {
            fKey(fk, index);
        }
    }
}
