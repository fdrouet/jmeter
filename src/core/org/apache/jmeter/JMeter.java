/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.jmeter;


import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.util.*;
import java.util.jar.*;

import javax.swing.*;

import org.apache.avalon.excalibur.cli.*;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.*;
import org.apache.jmeter.control.gui.*;
import org.apache.jmeter.engine.*;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.document.JMeterDocumentManager;
import org.apache.jmeter.gui.action.*;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.TestPlanTreeModel;
import org.apache.jmeter.plugin.*;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.save.*;
import org.apache.jmeter.testelement.*;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;


/**
 * @author mstover
 * @author <a href="mailto:oliver@tuxerra.com">Oliver Rossmueller</a>
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class JMeter implements JMeterPlugin, TranslationTableProvider
{

    transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
        "jmeter");

    private final static int PROPFILE_OPT = 'p';
    private final static int TESTFILE_OPT = 't';
    private final static int LOGFILE_OPT = 'l';
    private final static int NONGUI_OPT = 'n';
    protected static final int HELP_OPT = 'h';
    protected static final int VERSION_OPT = 'v';
    protected static final int SERVER_OPT = 's';
    protected static final int PROXY_HOST = 'H';
    protected static final int PROXY_PORT = 'P';
    protected static final int PROXY_USERNAME = 'u';
    protected static final int PROXY_PASSWORD = 'a';

    private static final String JMETER_HOME = "jmeter.home";

    /**
     *  Define the understood options. Each CLOptionDescriptor contains:
     * - The "long" version of the option. Eg, "help" means that "--help" will
     * be recognised.
     * - The option flags, governing the option's argument(s).
     * - The "short" version of the option. Eg, 'h' means that "-h" will be
     * recognised.
     * - A description of the option.
     */
    protected static final CLOptionDescriptor[] options =
        new CLOptionDescriptor[]{
            new CLOptionDescriptor(
                "help",
                CLOptionDescriptor.ARGUMENT_DISALLOWED,
                HELP_OPT,
                "print this message and exit"),
            new CLOptionDescriptor(
                "version",
                CLOptionDescriptor.ARGUMENT_DISALLOWED,
                VERSION_OPT,
                "print the version information and exit"),
            new CLOptionDescriptor(
                "propfile",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                PROPFILE_OPT,
                "the jmeter property file to use"),
            new CLOptionDescriptor(
                "testfile",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                TESTFILE_OPT,
                "the jmeter test(.jmx) file to run"),
            new CLOptionDescriptor(
                "logfile",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                LOGFILE_OPT,
                "the file to log samples to"),
            new CLOptionDescriptor(
                "logfile",
                CLOptionDescriptor.ARGUMENT_DISALLOWED,
                NONGUI_OPT,
                "run JMeter in nongui mode"),
            new CLOptionDescriptor(
                "server",
                CLOptionDescriptor.ARGUMENT_DISALLOWED,
                SERVER_OPT,
                "run the JMeter server"),
            new CLOptionDescriptor(
                "proxyHost",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                PROXY_HOST,
                "Set a proxy server for JMeter to use"),
            new CLOptionDescriptor(
                "proxyPort",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                PROXY_PORT,
                "Set proxy server port for JMeter to use"),
            new CLOptionDescriptor(
                "username",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                PROXY_USERNAME,
                "Set username for proxy server that JMeter is to use"),
            new CLOptionDescriptor(
                "password",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                PROXY_PASSWORD,
                "Set password for proxy server that JMeter is to use"),
        };

    public JMeter()
    {
    }

    /**
     * Starts up JMeter in GUI mode
     */
    public void startGui(CLOption testFile) throws IllegalUserActionException, IllegalAccessException, ClassNotFoundException, InstantiationException
    {
        JWindow splash = showSplash();

        PluginManager.install(this, true);
        installPlugins(true);

        TestPlanTreeModel treeModel = new TestPlanTreeModel();
        JMeterTreeListener treeLis = new JMeterTreeListener(treeModel);
        treeLis.setActionHandler(ActionRouter.getInstance());
        GuiPackage guiPack = GuiPackage.getInstance(treeLis, treeModel);
        org.apache.jmeter.gui.MainFrame main =
            new org.apache.jmeter.gui.MainFrame(
                ActionRouter.getInstance(),
                treeModel,
                treeLis);
        main.setTitle("Apache JMeter");
        main.setIconImage(JMeterUtils.getImage("jmeter.jpg").getImage());

        ComponentUtil.centerComponentInWindow(main, 80);


        ActionRouter.getInstance().actionPerformed(
            new ActionEvent(main, 1, CheckDirty.ADD_ALL));
        if (testFile == null) {
            JMeterDocumentManager.getInstance().newTestPlanDocument();
        } else
        {
            try
            {
                File file = new File(testFile.getArgument());
                JMeterDocumentManager.getInstance().loadDocument(file);
            } catch (Exception e)
            {
// todo: show error dialog
                log.error("Failure loading test file", e);
            }
        }
        main.show();
        splash.dispose();
        splash = null;
    }

    /**
     * Takes the command line arguments and uses them to determine how to startup JMeter.
     */
    public void start(String[] args)
    {

        CLArgsParser parser = new CLArgsParser(args, options);
        if (null != parser.getErrorString())
        {
            System.err.println("Error: " + parser.getErrorString());
            return;
        }
        try
        {
            initializeProperties(parser);
            setProxy(parser);
            if (parser.getArgumentById(VERSION_OPT) != null)
            {
                System.out.println(
                    "Apache JMeter, Copyright (c) 2002 The Apache Software Foundation");
                System.out.println("Version " + JMeterUtils.getJMeterVersion());
            } else if (parser.getArgumentById(HELP_OPT) != null)
            {
                System.out.println(
                    JMeterUtils.getResourceFileAsText("org/apache/jmeter/help.txt"));
            } else if (parser.getArgumentById(SERVER_OPT) != null)
            {
                startServer();
            } else if (parser.getArgumentById(NONGUI_OPT) == null)
            {
                startGui(parser.getArgumentById(TESTFILE_OPT));
            } else
            {
                startNonGui(
                    parser.getArgumentById(TESTFILE_OPT),
                    parser.getArgumentById(LOGFILE_OPT));
            }
        } catch (IllegalUserActionException e)
        {
            System.out.println(e.getMessage());
            System.out.println("Incorrect Usage");
            System.out.println(CLUtil.describeOptions(options).toString());
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("An error occurred: " + e.getMessage());
            System.exit(-1);
        }
    }

    /**
     * Sets a proxy server for the JVM if the command line arguments are specified.
     */
    private void setProxy(CLArgsParser parser) throws IllegalUserActionException
    {
        if (parser.getArgumentById(PROXY_USERNAME) != null)
        {
            if (parser.getArgumentById(PROXY_PASSWORD) != null)
            {
                Authenticator.setDefault(new ProxyAuthenticator(
                    parser.getArgumentById(PROXY_USERNAME).getArgument(),
                    parser.getArgumentById(PROXY_PASSWORD).getArgument()));
            } else
            {
                Authenticator.setDefault(new ProxyAuthenticator(
                    parser.getArgumentById(PROXY_USERNAME).getArgument(),
                    ""));
            }
        }
        if (parser.getArgumentById(PROXY_HOST) != null
            && parser.getArgumentById(PROXY_PORT) != null)
        {
            System.setProperty(
                "http.proxyHost",
                parser.getArgumentById(PROXY_HOST).getArgument());
            System.setProperty(
                "https.proxyHost",
                parser.getArgumentById(PROXY_HOST).getArgument());
            System.setProperty(
                "http.proxyPort",
                parser.getArgumentById(PROXY_PORT).getArgument());
            System.setProperty(
                "https.proxyPort",
                parser.getArgumentById(PROXY_PORT).getArgument());
        } else if (
            parser.getArgumentById(PROXY_HOST) != null
            || parser.getArgumentById(PROXY_PORT) != null)
        {
            throw new IllegalUserActionException(
                JMeterUtils.getResString("proxy_cl_error"));
        }
    }

    private void initializeProperties(CLArgsParser parser)
    {
        if (parser.getArgumentById(PROPFILE_OPT) != null)
        {
            JMeterUtils.getProperties(parser.getArgumentById(PROPFILE_OPT).getArgument());
        } else
        {
            JMeterUtils.getProperties(NewDriver.getJMeterDir() +
                                      File.separator + "bin" + File.separator + "jmeter.properties");
        }
        JMeterUtils.setJMeterHome(NewDriver.getJMeterDir());
    }

    public void startServer()
    {
        try
        {
            RemoteJMeterEngine engine = new RemoteJMeterEngineImpl();
            while (true)
            {
                Thread.sleep(Long.MAX_VALUE);
            }
        } catch (Exception ex)
        {
            log.error("", ex);
        }
    }

    public void startNonGui(CLOption testFile, CLOption logFile)
        throws IllegalUserActionException, IllegalAccessException, ClassNotFoundException, InstantiationException
    {
        JMeter driver = new JMeter();
        PluginManager.install(this, false);

        if (testFile == null)
        {
            throw new IllegalUserActionException();
        }
        if (logFile == null)
        {
            driver.run(testFile.getArgument(), null);
        } else
        {
            driver.run(testFile.getArgument(), logFile.getArgument());
        }
    }

    private void run(String testFile, String logFile)
    {
        FileInputStream reader = null;
        try
        {
            File f = new File(testFile);
            if (!f.exists() || !f.isFile())
            {
                println("Could not open " + testFile);
                return;
            }

            reader = new FileInputStream(f);

            // todo: shold never happen, but what if the element is not a NamedTestElement?
            NamedTestElement tree = (NamedTestElement)SaveService.loadSubTree(reader);
            if (logFile != null)
            {
                ResultCollector logger = new ResultCollector();
                logger.setFilename(logFile);
                tree.addChildElement(logger);
            }
            // todo: make ListenToTest a test element?
//            tree.addChildElement(new ListenToTest());
            println("Created the tree successfully");
            StandardJMeterEngine engine = new StandardJMeterEngine();
// todo: implement
//            engine.configure(tree);
            println("Starting the test");
            engine.runTest();

        } catch (Exception e)
        {
            System.out.println("Error in NonGUIDriver" + e.getMessage());
            log.error("", e);
        }
    }

    /**
     * Listen to test and exit program after test completes, after a 5 second delay to give listeners
     * a chance to close out their files.
     */
    private class ListenToTest implements TestListener, Runnable
    {

        public void testEnded(String host)
        {
        }

        public void testEnded()
        {
            Thread stopSoon = new Thread(this);
            stopSoon.start();
        }

        public void testStarted(String host)
        {
        }

        public void testStarted()
        {
            log.info(JMeterUtils.getResString("running_test"));
        }

        /**
         * This is a hack to allow listeners a chance to close their files.  Must implement
         * a queue for sample responses tied to the engine, and the engine won't deliver testEnded
         * signal till all sample responses have been delivered.  Should also improve performance of
         * remote JMeter testing.
         */
        public void run()
        {
            try
            {
                Thread.sleep(5000);
            } catch (InterruptedException e)
            {
            }
            System.exit(0);
        }
    }

    private static void println(String str)
    {
        System.out.println(str);
    }




    private void installPlugins(boolean useGui)
    {
        String home = System.getProperty(JMETER_HOME);
        String pluginDir;

        if (home == null)
        {
            home = ".";
        }

        pluginDir = home + "/plugin";
        File dir = new File(pluginDir);
        File[] jars = dir.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".jar");
            }
        });

        Map urls = new HashMap();
        String plugin;

        for (int i = 0; i < jars.length; i++)
        {
            if ((plugin = checkPlugin(jars[i])) != null)
            {
                try
                {
                    urls.put(plugin, jars[i].toURL());
                } catch (MalformedURLException e)
                {
                    // todo: log
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                }
            }
        }
        PluginManager.installPlugins(urls, useGui);

    }

    private String checkPlugin(File jar)
    {
        try
        {
            JarFile jarFile = new JarFile(jar);
            Enumeration entries = jarFile.entries();

            try
            {
                while (entries.hasMoreElements())
                {
                    JarEntry entry = (JarEntry)entries.nextElement();
                    String name = entry.getName();
                    if (name.endsWith("Plugin.class"))
                    {
                        return name.substring(0, name.length() - 6).replace('/', '.');
                    }
                }
            } finally
            {
                jarFile.close();
            }
            return null;
        } catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }


    public String[][] getIconMappings()
    {
        return new String[][]{
            {"DIRTY_TAB", "org/apache/jmeter/images/dirty_tab.png"},
            {TestPlan.class.getName() + "_TAB", "org/apache/jmeter/images/beaker_tab.png"},
            {TestPlan.class.getName(), "org/apache/jmeter/images/beaker.gif"},
            {org.apache.jmeter.threads.ThreadGroup.class.getName(), "org/apache/jmeter/images/thread.gif"},
            {AbstractListenerElement.class.getName(), "org/apache/jmeter/images/meter.png"},
            {ConfigTestElement.class.getName(), "org/apache/jmeter/images/testtubes.png"},
            {GenericController.class.getName(), "org/apache/jmeter/images/knob.gif"},
            {WorkBench.class.getName(), "org/apache/jmeter/images/clipboard.gif"},
            {AbstractSampler.class.getName(), "org/apache/jmeter/images/pipet.png"}
        };
    }

    public Class[][] getGuiMappings()
    {
        return new Class[][]{
            {TestPlan.class, TestPlanGui.class},
            {org.apache.jmeter.threads.ThreadGroup.class, ThreadGroupGui.class},
            {GenericController.class, LogicControllerGui.class},
            {LoopController.class, LoopControlPanel.class}
        };
    }

    public Class[] getElementClasses()
    {
        return new Class[]{
            TestPlan.class,
            org.apache.jmeter.threads.ThreadGroup.class,
            GenericController.class,
            LoopController.class,
        };
    }


    public Class[] getJavaSamplerClientClasses()
    {
        return new Class[]{
        };
    }


    public String[][] getResourceBundles()
    {
        return new String[0][];
    }


    public Map getTranslationTable()
    {
        Map translationTable = new HashMap();

        translationTable.put("TestElement.gui_class", null);
        translationTable.put("TestElement.test_class", null);
        translationTable.put("TestElement.name", new PropertyNameTranslator(NamedTestElement.NAME, false, true));
        translationTable.put("Arguments.arguments", new PropertyNameTranslator(Arguments.ARGUMENTS));
        translationTable.put("TestPlan.thread_groups", null);
        translationTable.put("TestPlan.functional_mode", new BooleanPropertyTranslator(TestPlan.FUNCTIONAL_MODE));
        translationTable.put("TestPlan.user_defined_variables", new PropertyNameTranslator(TestPlan.USER_DEFINED_VARIABLES));
        translationTable.put("ThreadGroup.num_threads", new IntegerPropertyTranslator(org.apache.jmeter.threads.ThreadGroup.NUMBER_OF_THREADS));
        translationTable.put("ThreadGroup.ramp_time", new IntegerPropertyTranslator(org.apache.jmeter.threads.ThreadGroup.RAMP_UP_PERIOD));
        translationTable.put("ThreadGroup.main_controller", null);
        translationTable.put("LoopController.loops", new IntegerPropertyTranslator(LoopController.LOOP_COUNT, true));
        translationTable.put("LoopController.continue_forever", new BooleanPropertyTranslator(LoopController.LOOP_FOREVER, true));
        return translationTable;
    }

    private JWindow showSplash()
    {

        JWindow splash = new JWindow();
        JPanel content = (JPanel)splash.getContentPane();
        SplashPanel splashPanel = new SplashPanel();

        // set the window's bounds, centering the window
        int width = 400;
        int height = 250;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - width) / 2;
        int y = (screen.height - height) / 2;
        splash.setBounds(x, y, width, height);
        splash.setBackground(Color.white);
        content.setBackground(Color.white);
        content.setBorder(BorderFactory.createLineBorder(Color.black, 1));

        JLabel copyright = new JLabel("Copyright 2001-2003, Apache Software Foundation", JLabel.CENTER);
        JPanel copyrightPanel = new JPanel();
        copyrightPanel.setLayout(new FlowLayout());
        copyrightPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
        copyrightPanel.setBackground(Color.white);
        copyright.setFont(new Font("Sans-Serif", Font.PLAIN, 12));
        copyright.setBackground(Color.white);
        copyrightPanel.add(copyright);
        content.add(splashPanel, BorderLayout.CENTER);
        content.add(copyrightPanel, BorderLayout.SOUTH);

        splash.setVisible(true);
        return splash;
    }

    class SplashPanel extends JPanel
    {

        Image image;
        int xpos;
        int ypos;

        SplashPanel()
        {
            image = JMeterUtils.getImage("jmeter.jpg").getImage();

            setSize(400, 250);
            setBackground(Color.white);
            xpos = (getSize().width - image.getWidth(this)) / 2;
            ypos = (getSize().height - image.getHeight(this)) / 2;
        }

        public void paint(Graphics g)
        {
//            super.paint(g);
            if (image != null)
            {
                g.drawImage(image, xpos, ypos, this);
            }
        }
    }
}