/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mediathek;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import mediathek.daten.Daten;
import mediathek.res.GetIcon;
import mediathek.tool.GuiFunktionen;
import mediathek.tool.ListenerMediathekView;
import mediathek.tool.Log;
import msearch.filmeSuchen.MSearchListenerFilmeLadenEvent;

/**
 *
 * @author emil
 */
public class MVStatusBar_Win_Linux extends MVStatusBar {

    private ImageIcon backImage1 = GetIcon.getIcon("Statusbar1.png");
    private ImageIcon backImage2 = GetIcon.getIcon("Statusbar2.png");
    private Daten daten;

    public MVStatusBar_Win_Linux(Daten dd) {
        daten = dd;
        initComponents();
        init();
    }

    private void init() {
        daten.mediathekGui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                updateUI();
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                updateUI();
            }
        });
        jButtonStop.setIcon(GetIcon.getIcon("stop_16.png"));
        setBackground(new java.awt.Color(204, 204, 204));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        hideProgressIndicators();
        jLabelStatusLinks.setMinimumSize(new Dimension(25, 25));
        jLabelRechts.setMinimumSize(new Dimension(25, 25));
        jButtonStop.setIcon(GetIcon.getIcon("close_15.png"));
        jButtonStop.setToolTipText("Abbrechen");
        jButtonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Daten.filmeLaden.setStop(true);
            }
        });
        ListenerMediathekView.addListener(new ListenerMediathekView(ListenerMediathekView.EREIGNIS_TIMER, MVStatusBar_Win_Linux.class.getSimpleName()) {
            @Override
            public void ping() {
                try {
                    if (!stopTimer) {
                        setInfoRechts();
                    }
                } catch (Exception ex) {
                    Log.fehlerMeldung(936251087, Log.FEHLER_ART_PROG, MVStatusBar_Mac.class.getName(), ex);
                }
            }
        });

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (daten.mediathekGui.isActive()) {
            g.drawImage(backImage1.getImage(), 0, 0, this.getWidth(), this.getHeight(), this);
        } else {
            g.drawImage(backImage2.getImage(), 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }

    @Override
    public void setTextLeft(StatusbarIndex i, String text) {
        displayListForLeftLabel.put(i, text);
        setIndexForCenterDisplay(currentIndex);
    }

    @Override
    public void setIndexForCenterDisplay(StatusbarIndex i) {
        currentIndex = i;
        String displayString = displayListForLeftLabel.get(i);
        jLabelStatusLinks.setText(displayString);
    }

    @Override
    public void updateProgressBar(MSearchListenerFilmeLadenEvent event) {
        stopTimer = true;
        jProgressBar1.setVisible(true);
        jButtonStop.setVisible(true);
        if (event.max == 0) {
            jProgressBar1.setIndeterminate(true);
            jProgressBar1.setMaximum(0);
            jProgressBar1.setMinimum(0);
            jProgressBar1.setValue(0);
            jProgressBar1.setStringPainted(false);
        } else {
            jProgressBar1.setIndeterminate(false);
            jProgressBar1.setMaximum(event.max);
            jProgressBar1.setMinimum(0);
            jProgressBar1.setValue(event.progress);
            jProgressBar1.setStringPainted(true);
        }
        jLabelRechts.setText(GuiFunktionen.textLaenge(60, event.text, true /* mitte */, true /*addVorne*/));
    }

    @Override
    public void hideProgressIndicators() {
        stopTimer = false;
        jProgressBar1.setVisible(false);
        jButtonStop.setVisible(false);
        setInfoRechts();
    }

    private void setInfoRechts() {
        // Text rechts: alter/neuladenIn anzeigen
        String textRechts = "Filmliste erstellt: ";
        textRechts += Daten.listeFilme.genDate();
        textRechts += " Uhr  ";
        int sekunden = Daten.listeFilme.alterFilmlisteSek();
        if (sekunden != 0) {
            textRechts += "||  Alter: ";
            int minuten = sekunden / 60;
            String sek = String.valueOf(sekunden % 60);
            String min = String.valueOf(minuten % 60);
            String stu = String.valueOf(minuten / 60);
            while (sek.length() < 2) {
                sek = "0" + sek;
            }
            while (min.length() < 2) {
                min = "0" + min;
            }
            while (stu.length() < 2) {
                stu = "0" + stu;
            }
            textRechts += stu + ":" + min + ":" + sek + " ";
        }
        // Infopanel setzen
        jLabelRechts.setText(textRechts);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelStatusLinks = new javax.swing.JLabel();
        jLabelRechts = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jButtonStop = new javax.swing.JButton();

        jLabelStatusLinks.setText("jLabel2");
        jLabelStatusLinks.setMaximumSize(new java.awt.Dimension(60, 60));
        jLabelStatusLinks.setMinimumSize(new java.awt.Dimension(60, 60));

        jLabelRechts.setText("jLabel1");

        jButtonStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediathek/res/stop_16.png"))); // NOI18N
        jButtonStop.setToolTipText("Abbrechen");
        jButtonStop.setIconTextGap(1);
        jButtonStop.setMargin(new java.awt.Insets(0, 10, 0, 10));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelStatusLinks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 260, Short.MAX_VALUE)
                .addComponent(jLabelRechts)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonStop)
                .addGap(6, 6, 6))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabelStatusLinks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelRechts)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonStop))
                .addGap(5, 5, 5))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButtonStop, jProgressBar1});

    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonStop;
    private javax.swing.JLabel jLabelRechts;
    private javax.swing.JLabel jLabelStatusLinks;
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables
}
