package jup.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.BlockingQueue;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import jup.event.*;
import jup.model.JupFile;
import jup.model.ScreenData;
import jup.defaults.*;


public class View
{
	/** okno programu */
	private JupFrame frame;
	/** pasek statusu programu*/
	private JLabel statusLabel;
	/** kolejka zdarzeñ przesy³anych do kontrolera */
	private static BlockingQueue<JupEvent> blockingQueue;
	
	/** dane wyœwietlane w tabeli plików */
	private DefaultTableModel tableModel = new DefaultTableModel(JupDefaults.COLUMN_NAMES, 0);
	
	public View(final BlockingQueue<JupEvent> bq, final ScreenData sd)
	{
		blockingQueue = bq;
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				frame = new JupFrame();
				refresh(sd);
			}
		});
	}
	
	public void refresh(final ScreenData sd)
	{
		SwingUtilities.invokeLater(new Runnable()
		{		
			@Override
			public void run()
			{
				frame.drawTable(sd);
				frame.updateStatus(sd);
			}
		});
	}
	
	private class JupFrame extends JFrame
	{
		private static final long serialVersionUID = -7830784355551599029L;
		private final JScrollPane table;
		
		private JupFrame()
		{
			super("JUP (KS)");
			setLayout(new BorderLayout());
			setLocationByPlatform(true);
			
			setJMenuBar(createMenu());
			
			//add(createToolbar(), BorderLayout.PAGE_START);
			
			table = createTable();
			
			add(table, BorderLayout.CENTER);
			//add(createInfoPanel(), BorderLayout.LINE_END);
			
			ActionListener checkEditionsListener = new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					try
					{
						blockingQueue.put(new EditionsEvent());
					} catch (Exception ex)
					{
						ex.printStackTrace();
						throw new RuntimeException(ex);
					}
				}
			};
			
			/** obs³uga zamykania programu poprzez naciœniêcie X */
			this.addWindowListener(new WindowAdapter()
			{
			    @Override
			    public void windowClosing(WindowEvent e)
			    {
					try
					{
						blockingQueue.put(new ExitEvent());
						JOptionPane.showMessageDialog(frame, "background task running\nforce quit?");
					} catch (Exception ex)
					{
						ex.printStackTrace();
						throw new RuntimeException(ex);
					}
			    }
			});
			
			pack();
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			//setResizable(false);
			setSize(1000, 400);
			setVisible(true);	
			
			
			JButton button = new JButton("?");			
			button.addActionListener(checkEditionsListener);
			
			button.setPreferredSize(new Dimension(40, 40));
			add(button, BorderLayout.LINE_END);
			
			statusLabel = createStatusLabel();
			add(statusLabel, BorderLayout.PAGE_END);
		}
		
		

		private void drawTable(ScreenData sd)
		{
			//usuwam istniej¹ce wiersze
			if (tableModel.getRowCount() > 0)
			{
			    for (int i = tableModel.getRowCount() - 1; i > -1; i--)
			    {
			    	tableModel.removeRow(i);
			    }
			}
			
			//uzupe³niam o aktualne wiersze tabelê JTable
			for (JupFile el : sd.tableData)
			{
				tableModel.addRow(new Object[]{el.getName(), el.getPath(), el.getStatus(), el.getChecksum(), el.getSize(), "X"});
			}
		}
		
		private void updateStatus(ScreenData sd)
		{
			statusLabel.setText(sd.getStatusText());
		}

		private final JMenuBar createMenu()
		{
			ActionListener addListener = new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						// okno wyboru pliku
						JFileChooser fileChooser = new JFileChooser();
						fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						int returnValue = fileChooser.showOpenDialog(null);
						if (returnValue == JFileChooser.APPROVE_OPTION)
						{
							File selectedFile = fileChooser.getSelectedFile();
							System.out.println("View: dodaje AddFileEvent(" + selectedFile.getParent() + ", " + selectedFile.getName() + ")");
							blockingQueue.put(new AddFileEvent(selectedFile.getParent(), selectedFile.getName()));
						}
						else
						{
							System.out.println("View: b³¹d wybrania pliku, nie dodano eventu");
						}
					} catch(Exception ex)
					{
						ex.printStackTrace();
						throw new RuntimeException(ex);
					}			
				}
			};
			
			JMenu fileMenu = new JMenu("File");
			JMenuItem addMenuItem = new JMenuItem("Add");
			addMenuItem.addActionListener(addListener);
			
			fileMenu.add(addMenuItem);
			
			JMenuBar menuBar = new JMenuBar();
			menuBar.add(fileMenu);
			return menuBar;
		}
		
		private final JScrollPane createTable()
		{
			JTable table = new JTable(tableModel);
			JScrollPane scrollPane = new JScrollPane(table);
			table.setFillsViewportHeight(true);
			
			table.addMouseListener(new MouseAdapter()
			{
			    public void mouseClicked(MouseEvent evnt)
			    {
			        if (evnt.getClickCount() == 1 && table.getSelectedRow() >= 0)
			        {
			        	if (table.getSelectedColumn()!=5)
			        	{
							try
							{
								// okno wyboru pliku
								JFileChooser fileChooser = new JFileChooser();
								fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
								int returnValue = fileChooser.showOpenDialog(null);
								if (returnValue == JFileChooser.APPROVE_OPTION)
								{
									File selectedDir = fileChooser.getSelectedFile();
									System.out.println("View: dodaje DownloadFileEvent " + selectedDir.getPath() + " dla " + table.getValueAt(table.getSelectedRow(), 0));
									blockingQueue.put(new DownloadFileEvent(table.getValueAt(table.getSelectedRow(), 1).toString(), table.getValueAt(table.getSelectedRow(), 0).toString(), selectedDir.getPath()));
								}
								else
								{
									System.out.println("View: b³¹d wybrania folderu zapisu, nie dodano eventu");
								}
							} catch(Exception ex)
							{
								//ex.printStackTrace();
								JOptionPane.showMessageDialog(null, "program is busy\ntry again");
								//throw new RuntimeException(ex);
							}	
			        	}
			        	
			        	//przycisk usuwania
			        	else if (table.getSelectedColumn()==5)
			        	{
							try
							{
								blockingQueue.put(new DeleteFileEvent(table.getValueAt(table.getSelectedRow(), 1).toString(), table.getValueAt(table.getSelectedRow(), 0).toString()));
							} catch (InterruptedException e)
							{
								JOptionPane.showMessageDialog(null, "program is busy\ntry again");
								//e.printStackTrace();
							}

			        	}
			        }
			     }
			});
			
			return scrollPane;
		}
		
		private JLabel createStatusLabel()
		{
			JLabel label = new JLabel("program status");
			label.setHorizontalTextPosition(JLabel.LEFT);
			
			return label;
		}
	}
}
