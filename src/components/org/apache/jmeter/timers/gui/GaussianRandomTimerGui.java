/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.timers.gui;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.apache.jmeter.gui.GUIFactory;
import org.apache.jmeter.gui.util.DoubleFieldDocumentListener;
import org.apache.jmeter.gui.util.JMeterGridBagConstraints;
import org.apache.jmeter.gui.util.LongFieldDocumentListener;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.GaussianRandomTimer;
import org.apache.jmeter.timers.RandomTimer;
import org.apache.jmeter.util.JMeterUtils;


/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 * @author    Michael Stover
 * @author <a href="mailto:seade@backstagetech.com.au">Scott Eade</a>
 * @author  <a href="mailto:oliver@tuxerra.com">Oliver Rossmueller</a>
 * @created   $Date$
 * @version   1.0
 ***************************************/

public class GaussianRandomTimerGui extends AbstractTimerGui implements KeyListener
{

    private JTextField delayInput;
    private JTextField rangeInput;
    private JLabel delayLabel;
    private JLabel rangeLabel;
    private JLabel millisecondsLabel1;
    private JLabel millisecondsLabel2;


    public GaussianRandomTimerGui()
    {
    }


    /****************************************
     * !ToDo (Method description)
     *
     *@param e        !ToDo (Parameter description)
     *@param thrower  !ToDo (Parameter description)
     ***************************************/
    public static void error(Exception e, JComponent thrower)
    {
        JOptionPane.showMessageDialog(thrower, e, "Error", JOptionPane.ERROR_MESSAGE);
    }


    public TestElement createTestElement()
    {
        RandomTimer timer = new GaussianRandomTimer();
        this.configureTestElement(timer);
        timer.setDelay(Long.parseLong(delayInput.getText()));
        timer.setRange(Double.parseDouble(rangeInput.getText()));
        return timer;
    }


    public void configure(TestElement element)
    {
        super.configure(element);
        // todo: variable substitution
        delayInput.setText(String.valueOf(((GaussianRandomTimer)element).getDelay()));
        rangeInput.setText(String.valueOf(((GaussianRandomTimer)element).getRange()));
    }


    public String getStaticLabel()
    {
        return "gaussian_timer_title";
    }


    protected void initComponents()
    {
        super.initComponents();

        JPanel delayPanel = GUIFactory.createPanel();
        delayPanel.setLayout(new GridBagLayout());
        JMeterGridBagConstraints constraints = new JMeterGridBagConstraints();

        rangeLabel = new JLabel(JMeterUtils.getResString("gaussian_timer_range"));
        rangeLabel.setName("gaussian_timer_range");
        delayPanel.add(rangeLabel, constraints);
        rangeInput = new JTextField(6);
        rangeInput.addKeyListener(this);
        rangeInput.setHorizontalAlignment(JTextField.RIGHT);
        rangeInput.getDocument().addDocumentListener(new DoubleFieldDocumentListener(RandomTimer.RANGE, rangeInput, this));
        constraints = constraints.incrementX();
        delayPanel.add(rangeInput, constraints);
        millisecondsLabel1 = new JLabel(JMeterUtils.getResString("milliseconds"));
        millisecondsLabel1.setName("milliseconds");
        constraints = constraints.incrementX();
        delayPanel.add(millisecondsLabel1, constraints);
        delayLabel = new JLabel(JMeterUtils.getResString("gaussian_timer_delay"));
        delayLabel.setName("gaussian_timer_delay");
        constraints = constraints.nextRow();
        delayPanel.add(delayLabel, constraints);
        delayInput = new JTextField(6);
        delayInput.addKeyListener(this);
        delayInput.setHorizontalAlignment(JTextField.RIGHT);
        delayInput.getDocument().addDocumentListener(new LongFieldDocumentListener(RandomTimer.DELAY, delayInput, this));
        constraints = constraints.incrementX();
        delayPanel.add(delayInput, constraints);
        millisecondsLabel2 = new JLabel(JMeterUtils.getResString("milliseconds"));
        millisecondsLabel2.setName("milliseconds");
        constraints = constraints.incrementX();
        delayPanel.add(millisecondsLabel2, constraints);
        Component filler = Box.createHorizontalGlue();
        constraints = constraints.incrementX();
        constraints.fillHorizontal(1.0);
        delayPanel.add(filler, constraints);

        add(delayPanel);
    }


    // todo: extract to utiility class
    public void keyReleased(KeyEvent e)
    {
        Object component = e.getComponent();

        if (component == delayInput) {
            try {
                Long.parseLong(delayInput.getText());
            } catch (NumberFormatException nfe) {
                if (delayInput.getText().length() > 0) {
                    JOptionPane.showMessageDialog(this, "You must enter a valid number",
                                                  "Invalid data", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else if (component == rangeInput) {
            try {
                Double.parseDouble(rangeInput.getText());
            } catch (NumberFormatException nfe) {
                if (rangeInput.getText().length() > 0) {
                    JOptionPane.showMessageDialog(this, "You must enter a valid number",
                                                  "Invalid data", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }


    public void keyPressed(KeyEvent e)
    {
    }


    public void keyTyped(KeyEvent e)
    {
    }


    // LocaleChangeListener methdo
    public void localeChanged(org.apache.jmeter.util.LocaleChangeEvent event)
    {
        super.localeChanged(event);
        updateLocalizedStrings(new JComponent[]{delayLabel, rangeLabel, millisecondsLabel1, millisecondsLabel2});
    }

}
