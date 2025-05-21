import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Tab_Panel {
    private final TableRowSorter<DefaultTableModel> sorter;
    private final DefaultTableModel model;
    private final CSVHandler csvHandler;

    public Tab_Panel(TableRowSorter<DefaultTableModel> sorter, DefaultTableModel model, CSVHandler csvHandler) {
        this.sorter = sorter;
        this.model = model;
        this.csvHandler = csvHandler;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel();
        JTextField srchfields = new JTextField();
        JComboBox<String> srchBy = new JComboBox<>();
        JLabel srchlbl = new JLabel("Search By:");

        String[] srch = csvHandler.getHeaders();
        for (String s : srch) {
            srchBy.addItem(s);
        }

        srchfields.addKeyListener(srchKeyListener(srchfields, srchBy));

        return Layout.SearchPanelLayout(searchPanel, srchfields, srchlbl, srchBy);
    }

    protected JPanel createTabPanel(String name, JScrollPane sp) {
        JPanel tabPanel = new JPanel();
        JLabel tabTitle;

            tabTitle = new JLabel(name + " Table");
            tabTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JButton editTable = new JButton("Edit Table");
        JButton refresh = new JButton("Refresh Table");
        JPanel searchPanel = createSearchPanel();

        tabPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        editTable.addActionListener(_ -> editTableActionPerformed(tabPanel));
        refresh.addActionListener(_ -> GUI.loadData(model, csvHandler));


        return Layout.TabPanelLayout(tabPanel, sp, tabTitle, editTable, searchPanel, refresh);
    }

    private KeyAdapter srchKeyListener(JTextField search, JComboBox<String> srchBy) {
        return new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String Search = search.getText().trim().toLowerCase();
                String SrchBy = (String) srchBy.getSelectedItem();

                if (SrchBy == null) {
                    return;
                }

                int column = -1;
                String[] colName = csvHandler.getHeaders();
                for (int i = 0; i <colName.length; i++) {
                    if (colName[i].equals(SrchBy)) {
                        column = i;
                        break;
                    }
                }
                if (column == -1) {
                    return;
                }
                sorter.setRowFilter(RowFilter.regexFilter(("(?i)") + Search, column));
            }

        };


    }

    private void editTableActionPerformed(JPanel tabPanel) {
        Component parent = tabPanel.getParent();
        while (!(parent instanceof JPanel) && parent != null) {
            parent = parent.getParent();
        }
        if (parent != null) {
            for (Component c : ((JPanel) parent).getComponents()) {
                if (c instanceof JPanel && c.getPreferredSize().width < 300) {
                    for (Component inner : ((JPanel) c).getComponents()) {
                        if (inner instanceof JPanel) {
                            for (Component field : ((JPanel) inner).getComponents()) {
                                field.setEnabled(true);
                            }
                        } else if (inner instanceof JButton) {
                            inner.setEnabled(true);
                        }
                    }
                }
            }
        }
    }
}
