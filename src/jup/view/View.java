package jup.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.BlockingQueue;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import jup.event.JupEvent;
import jup.event.PrintFileListEvent;
import jup.event.AddFileEvent;
import jup.model.ScreenData;
import jup.defaults.*;


public class View
{
	/** okno programu */
	private JupFrame frame;
	/** kolejka zdarzeñ przesy³anych z widoku do kontrolera */
	private static BlockingQueue<JupEvent> blockingQueue;
	
	/** dane wyœwietlane w tabeli plików */
	private DefaultTableModel tableModel = new DefaultTableModel(JupDefaults.COLUMN_NAMES, 0);
	/** szczegó³y zaznaczonego pliku*/
	/** TODO ?? */
	
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
			
			add(createToolbar(), BorderLayout.PAGE_START);
			
			table = createTable();
			
			add(table, BorderLayout.CENTER);
			//add(createInfoPanel(), BorderLayout.LINE_END);
			
			ActionListener test2Listener = new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					try
					{
						System.out.println("View: dodaje PrintFileListEvent");
						blockingQueue.put(new PrintFileListEvent());
					} catch (Exception ex)
					{
						ex.printStackTrace();
						throw new RuntimeException(ex);
					}
				}
			};
			
			
			JButton button = new JButton("Select File");			
			button.addActionListener(test2Listener);
			
			button.setPreferredSize(new Dimension(40, 40));
			add(button, BorderLayout.LINE_END);
			
			
			
			
			pack();
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			//setResizable(false);
			setVisible(true);	
		}
		
		private void drawTable(ScreenData sd)
		{
			tableModel.addRow(sd.tableData);
		}

		private final JMenuBar createMenu()
		{
			ActionListener testListener = new ActionListener()
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
			addMenuItem.addActionListener(testListener);
			
			fileMenu.add(addMenuItem);
			
			JMenuBar menuBar = new JMenuBar();
			menuBar.add(fileMenu);
			return menuBar;
		}

		private final JPanel createInfoPanel()
		{
			// TODO Auto-generated method stub
			return null;
		}

		private final JToolBar createToolbar()
		{
			// TODO wyj¹æ przydatne opcje - opcjonalnie
			JToolBar toolbar = new JToolBar();
			return toolbar;
		}
		
		private final JScrollPane createTable()
		{
			JTable table = new JTable(tableModel);
			JScrollPane scrollPane = new JScrollPane(table);
			table.setFillsViewportHeight(true);
			return scrollPane;
		}
	}
}
