/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License");  you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Original Code is Protege-2000.
 *
 * The Initial Developer of the Original Code is Stanford University. Portions
 * created by Stanford University are Copyright (C) 2007.  All Rights Reserved.
 *
 * Protege was developed by Stanford Medical Informatics
 * (http://www.smi.stanford.edu) at the Stanford University School of Medicine
 * with support from the National Library of Medicine, the National Science
 * Foundation, and the Defense Advanced Research Projects Agency.  Current
 * information about Protege can be obtained at http://protege.stanford.edu.
 * 
 * This file is a modified version of the AssertedInstancesLisPanel from
 * the Protege-OWL package. Modified by Tristan Davey, James Cook University
 * for the VisualisationTab project - 2011.
 *
 */

package au.edu.jcu.bus.it.tjd.vistab;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ListModel;

import edu.stanford.smi.protege.action.DeleteInstancesAction;
import edu.stanford.smi.protege.action.MakeCopiesAction;
import edu.stanford.smi.protege.action.ReferencersAction;
import edu.stanford.smi.protege.event.ClsAdapter;
import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.ClsListener;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.model.BrowserSlotPattern;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.resource.Colors;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.ui.ConfigureAction;
import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.ui.HeaderComponent;
import edu.stanford.smi.protege.ui.ListFinder;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.CreateAction;
import edu.stanford.smi.protege.util.Disposable;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.SimpleListModel;
import edu.stanford.smi.protege.util.ViewAction;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.ui.*;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import edu.stanford.smi.protegex.owl.ui.individuals.AssertedInstancesListPanel;
import edu.stanford.smi.protegex.owl.ui.individuals.InstancesList;
import edu.stanford.smi.protegex.owl.ui.individuals.MultiSlotPanel;

/**
 * The panel that holds the list of direct instances of one or more classes. If
 * only one class is chosen then you can also create new instances of this
 * class.
 *
 * @author Holger Knublauch  <holger@knublauch.com>
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public final class VisualisationInstancesListPanel extends AssertedInstancesListPanel implements Disposable {

    private Collection classes = Collections.EMPTY_LIST;

    private AllowableAction createAction;

    private AllowableAction createAnonymousAction;
    
    private AllowableAction copyAction;

    private AllowableAction deleteAction;

    private HeaderComponent header;

    private OWLLabeledComponent lc;

    private InstancesList list;

    private Collection listenedToInstances = new ArrayList();

    private OWLModel owlModel;

    private static final int SORT_LIMIT;

    private boolean showSubclassInstances = true;


    static {
        SORT_LIMIT = ApplicationProperties.getIntegerProperty("ui.DirectInstancesList.sort_limit", 1000);
    }


    private ClsListener _clsListener = new ClsAdapter() {
        @Override
        public void directInstanceAdded(ClsEvent event) {
            Instance instance = event.getInstance();
            if (!getModel().contains(instance)) {
                ComponentUtilities.addListValue(list, instance);
                instance.addFrameListener(_instanceFrameListener);
            }
        }


        @Override
        public void directInstanceRemoved(ClsEvent event) {
            removeInstance(event.getInstance());
        }
    };

    private FrameListener _clsFrameListener = new FrameAdapter() {
        @Override
        public void ownSlotValueChanged(FrameEvent event) {
            super.ownSlotValueChanged(event);
            //updateButtons();
        }
    };

    private FrameListener _instanceFrameListener = new FrameAdapter() {
        @Override
        public void browserTextChanged(FrameEvent event) {
            super.browserTextChanged(event);
            sort();
            repaint();
        }
    };


    public VisualisationInstancesListPanel(OWLModel owlModel) {
        super(owlModel);
        this.owlModel = owlModel;
        Action viewAction = createViewAction();

        list = new InstancesList(viewAction);

        lc = new OWLLabeledComponent(null, ComponentFactory.createScrollPane(list));
        //addButtons(viewAction, lc);

        /*
         * FIXME: TT: This search code does not handle the browser text of the individuals correctly.
         * Temporary fix: use the frames instance finder.         
         */
        /*
        ResultsViewModelFind findAlg = new DefaultIndividualFind(owlModel, Find.CONTAINS) {
            protected boolean isValidFrameToSearch(Frame f) {
                return (((SimpleListModel) list.getModel()).getValues()).contains(f) &&
                       super.isValidFrameToSearch(f);
            }

            public String getDescription() {
                return "Find Individual Of Selected Class";
            }
        };
        FindAction fAction = new FindInDialogAction(findAlg,
                                                    Icons.getFindInstanceIcon(),
                                                    list, true);

        ResourceFinder finder = new ResourceFinder(fAction);
        lc.setFooterComponent(finder);
        */
        
        lc.setFooterComponent(new ListFinder(list, ResourceKey.INSTANCE_SEARCH_FOR));

        lc.setBorder(ComponentUtilities.getAlignBorder());
        add(lc, BorderLayout.CENTER);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeader(), BorderLayout.NORTH);
        add(panel, BorderLayout.NORTH);

        setSelectable(list);
        addInstanceListeners();
        addClsListeners();
        // initializeShowSubclassInstances();
        lc.setHeaderLabel("Visualisation Instances");
    }


    private void updateLabel() {
        String text;
        Cls cls = getSoleAllowedCls();
        BrowserSlotPattern pattern = (cls == null) ? null : cls.getBrowserSlotPattern();
        if (pattern == null) {
            text = null;
        }
        else {
            // text = "Instances by ";
            if (pattern.isSimple()) {
                text = pattern.getFirstSlot().getBrowserText();
                if (Model.Slot.NAME.equals(text)) {
                    text = "Visualisation Instances";
                }
            }
            else {
                text = "multiple properties";
            }
        }
        lc.setHeaderLabel(text);
    }


    private HeaderComponent createHeader() {
        JLabel label = ComponentFactory.createLabel();
        String instanceBrowserLabel = LocalizedText.getText(ResourceKey.INSTANCE_BROWSER_TITLE);
        String forClassLabel = LocalizedText.getText(ResourceKey.CLASS_EDITOR_FOR_CLASS_LABEL);
        header = new HeaderComponent(instanceBrowserLabel, forClassLabel, label);
        header.setColor(Colors.getInstanceColor());
        return header;
    }


    private void fixRenderer() {
        FrameRenderer frameRenderer = (FrameRenderer) list.getCellRenderer();
        frameRenderer.setDisplayType(showSubclassInstances);
    }


    @Override
    protected void addButtons(Action viewAction, LabeledComponent c) {
        // c.addHeaderButton(createReferencersAction());
        c.addHeaderButton(createConfigureAction());
        c.addHeaderButton(createCreateAction());
        c.addHeaderButton(createCopyAction());
        c.addHeaderButton(createDeleteAction());
        c.addHeaderButton(createCreateAnonymousAction());
    }


    private void addClsListeners() {
        Iterator i = classes.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            cls.addClsListener(_clsListener);
            cls.addFrameListener(_clsFrameListener);
            addSubClsListener(cls);
        }
    }

    
    private void addSubClsListener(Cls listenClass) {
        Collection classCollection = listenClass.getDirectSubclasses();
        Iterator i = classCollection.iterator();
        while(i.hasNext()) {
            Cls nextClass = (Cls) i.next();
            nextClass.addClsListener(_clsListener);
            nextClass.addFrameListener(_clsFrameListener);
            addSubClsListener(nextClass);
        }
    }

    private void addInstanceListeners() {
        ListModel model = list.getModel();
        int start = list.getFirstVisibleIndex();
        int stop = list.getLastVisibleIndex();
        for (int i = start; i < stop; ++i) {
            Instance instance = (Instance) model.getElementAt(i);
            addInstanceListener(instance);

        }
    }


    private void removeInstanceListeners() {
        Iterator i = listenedToInstances.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            instance.removeFrameListener(_instanceFrameListener);
        }
        listenedToInstances.clear();
    }


    private void addInstanceListener(Instance instance) {
        instance.addFrameListener(_instanceFrameListener);
        listenedToInstances.add(instance);
    }


    @Override
    protected Action createCreateAction() {
        createAction = new CreateAction("Create instance", OWLIcons.getCreateIndividualIcon(OWLIcons.RDF_INDIVIDUAL)) {
            @Override
            public void onCreate() {
                if (!classes.isEmpty()) {
                    Instance instance = owlModel.createInstance(null, classes);
                    if (instance instanceof Cls) {
                        Cls newCls = (Cls) instance;
                        if (newCls.getDirectSuperclassCount() == 0) {
                            newCls.addDirectSuperclass(owlModel.getOWLThingClass());
                        }
                    }
                    list.setSelectedValue(instance, true);
                }
            }
        };
        return createAction;
    }

    
    @Override
    protected Action createCreateAnonymousAction() {
        createAnonymousAction = new CreateAction("Create anonymous instance", OWLIcons.getCreateIndividualIcon(OWLIcons.RDF_ANON_INDIVIDUAL)) {
            @Override
            public void onCreate() {
                if (!classes.isEmpty()) {
                	String name = owlModel.getNextAnonymousResourceName();
                    Instance instance = owlModel.createInstance(name, classes);
                    if (instance instanceof Cls) {
                        Cls newCls = (Cls) instance;
                        if (newCls.getDirectSuperclassCount() == 0) {
                            newCls.addDirectSuperclass(owlModel.getOWLThingClass());
                        }
                    }
                    list.setSelectedValue(instance, true);
                }
            }
        };
        return createAnonymousAction;
    }
    

    @Override
    protected Action createConfigureAction() {
        return new ConfigureAction() {
            @Override
            public void loadPopupMenu(JPopupMenu menu) {
                menu.add(createSetDisplaySlotAction());
                menu.add(createShowAllInstancesAction());
            }
        };
    }


    @Override
    protected JMenuItem createShowAllInstancesAction() {
        Action action = new AbstractAction("Show Subclass Instances") {
            @Override
            public void actionPerformed(ActionEvent event) {
                setShowAllInstances(!showSubclassInstances);
            }
        };
        JMenuItem item = new JCheckBoxMenuItem(action);
        item.setSelected(showSubclassInstances);
        return item;
    }

    //    private void initializeShowSubclassInstances() {
    //        showSubclassInstances = ApplicationProperties.getBooleanProperty(SHOW_SUBCLASS_INSTANCES, false);
    //        reload();
    //        fixRenderer();
    //    }


    private void setShowAllInstances(boolean b) {
        //showSubclassInstances = b;
        // ApplicationProperties.setBoolean(SHOW_SUBCLASS_INSTANCES, b);
        reload();
        fixRenderer();
    }


    @Override
    protected Cls getSoleAllowedCls() {
        Cls cls;
        if (classes.size() == 1) {
            cls = (Cls) CollectionUtilities.getFirstItem(classes);
        }
        else {
            cls = null;
        }
        return cls;
    }


    @Override
    protected JMenu createSetDisplaySlotAction() {
        JMenu menu = ComponentFactory.createMenu("Set Display Slot");
        boolean enabled = false;
        Cls cls = getSoleAllowedCls();
        if (cls != null) {
            BrowserSlotPattern pattern = cls.getBrowserSlotPattern();
            Slot browserSlot = (pattern != null && pattern.isSimple()) ? pattern.getFirstSlot() : null;
            Iterator i = cls.getVisibleTemplateSlots().iterator();
            while (i.hasNext()) {
                Slot slot = (Slot) i.next();
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(createSetDisplaySlotAction(slot));
                if (slot.equals(browserSlot)) {
                    item.setSelected(true);
                }
                menu.add(item);
                enabled = true;
            }
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(createSetDisplaySlotMultipleAction());
            if (browserSlot == null) {
                item.setSelected(true);
            }
            menu.add(item);
        }
        menu.setEnabled(enabled);
        return menu;
    }


    @Override
    protected Action createSetDisplaySlotAction(final Slot slot) {
        return new AbstractAction(slot.getBrowserText(), slot.getIcon()) {
            @Override
            public void actionPerformed(ActionEvent event) {
                getSoleAllowedCls().setDirectBrowserSlot(slot);
                updateLabel();
                repaint();
            }
        };
    }


    @Override
    protected Action createSetDisplaySlotMultipleAction() {
        return new AbstractAction("Multiple Slots...") {
            @Override
            public void actionPerformed(ActionEvent event) {
                Cls cls = getSoleAllowedCls();
                BrowserSlotPattern currentPattern = getSoleAllowedCls().getBrowserSlotPattern();
                MultiSlotPanel panel = new MultiSlotPanel(currentPattern, cls);
                int rval = ModalDialog.showDialog(VisualisationInstancesListPanel.this, panel, "Multislot Display Pattern",
                                                  ModalDialog.MODE_OK_CANCEL);
                if (rval == ModalDialog.OPTION_OK) {
                    BrowserSlotPattern pattern = panel.getBrowserTextPattern();
                    if (pattern != null) {
                        cls.setDirectBrowserSlotPattern(pattern);
                    }
                }
                updateLabel();
                repaint();
            }
        };
    }


    @Override
    protected Action createDeleteAction() {
        deleteAction = new DeleteInstancesAction(this);
        return deleteAction;
    }


    @Override
    protected Action createCopyAction() {
        copyAction = new MakeCopiesAction(ResourceKey.INSTANCE_COPY, this) {
            @Override
            protected Instance copy(Instance instance, boolean isDeep) {
                Instance copy = super.copy(instance, isDeep);
                setSelectedInstance(copy);
                return copy;
            }
        };
        return copyAction;
    }


    @Override
    protected Action createReferencersAction() {
        return new ReferencersAction(ResourceKey.INSTANCE_VIEW_REFERENCES, this);
    }


    @Override
    protected Action createViewAction() {
        return new ViewAction(ResourceKey.INSTANCE_VIEW, this) {
            @Override
            public void onView(Object o) {
                owlModel.getProject().show((Instance) o);
            }
        };
    }


    @Override
    public void dispose() {
        removeClsListeners();
        removeInstanceListeners();
    }


    @Override
    public JComponent getDragComponent() {
        return list;
    }


    private SimpleListModel getModel() {
        return (SimpleListModel) list.getModel();
    }


    private boolean isSelectionEditable() {
        boolean isEditable = true;
        Iterator i = getSelection().iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            if (!instance.isEditable()) {
                isEditable = false;
                break;
            }
        }
        return isEditable;
    }


    @Override
    public void onSelectionChange() {
        // Log.enter(this, "onSelectionChange");
        boolean editable = isSelectionEditable();
        ComponentUtilities.setDragAndDropEnabled(list, editable);
        //updateButtons();
    }


    private void removeInstance(Instance instance) {
        ComponentUtilities.removeListValue(list, instance);
        instance.removeFrameListener(_instanceFrameListener);
    }


    private void removeClsListeners() {
        Iterator i = classes.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            cls.removeClsListener(_clsListener);
            cls.removeFrameListener(_clsFrameListener);
            removeSubClsListener(cls);
        }
    }
    
    private void removeSubClsListener(Cls listenClass) {
        Collection classCollection = listenClass.getDirectSubclasses();
        Iterator i = classCollection.iterator();
        while(i.hasNext()) {
            Cls nextClass = (Cls) i.next();
            nextClass.removeClsListener(_clsListener);
            nextClass.removeFrameListener(_clsFrameListener);
            removeSubClsListener(nextClass);
        }
    }


    @Override
    public void setClses(Collection newClses) {
        removeClsListeners();
        classes = new ArrayList(newClses);
        list.setClasses(newClses);
        reload();
        //updateButtons();
        addClsListeners();
    }


    @Override
    public void reload() {
        removeInstanceListeners();
        Object selectedValue = list.getSelectedValue();
        Set instanceSet = new LinkedHashSet();
        Iterator i = classes.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            instanceSet.addAll(getInstances(cls));
        }
        List instances = new ArrayList(instanceSet);
        if (instances.size() <= SORT_LIMIT) {
            Collections.sort(instances, new FrameComparator());
        }
        getModel().setValues(instances);
        if (instances.contains(selectedValue)) {
            list.setSelectedValue(selectedValue, true);
        }
        else if (!instances.isEmpty()) {
            list.setSelectedIndex(0);
        }
        addInstanceListeners();
        updateLabel();
    }


    private void reloadHeader(Collection clses) {
        StringBuilder text = new StringBuilder();
        Icon icon = null;
        Iterator i = clses.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            if (icon == null) {
                icon = cls.getIcon();
            }
            if (text.length() != 0) {
                text.append(", ");
            }
            text.append(cls.getName());
        }
        JLabel label = (JLabel) header.getComponent();
        label.setText(text.toString());
        label.setIcon(icon);
    }


    private Collection getInstances(Cls cls) {
        Collection instances;
        if (showSubclassInstances) {
            instances = cls.getInstances();
        }
        else {
            instances = cls.getDirectInstances();
        }
        if (!owlModel.getProject().getDisplayHiddenFrames()) {
            instances = removeHiddenInstances(instances);
        }
        return instances;
    }


    private static Collection removeHiddenInstances(Collection instances) {
        Collection visibleInstances = new ArrayList(instances);
        Iterator i = visibleInstances.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            if (!instance.isVisible()) {
                i.remove();
            }
        }
        return visibleInstances;
    }


    @Override
    public void sort() {
        list.setListenerNotificationEnabled(false);
        Object selectedValue = list.getSelectedValue();
        List instances = new ArrayList(getModel().getValues());
        if (instances.size() <= SORT_LIMIT) {
            Collections.sort(instances, new FrameComparator());
        }
        getModel().setValues(instances);
        list.setSelectedValue(selectedValue);
        list.setListenerNotificationEnabled(true);
    }


    @Override
    public void setSelectedInstance(Instance instance) {
        list.setSelectedValue(instance, true);
        //updateButtons();
    }


    private void updateButtons() {
        Cls cls = (Cls) CollectionUtilities.getFirstItem(classes);        
        createAction.setEnabled(cls == null ? false : cls.isConcrete());
        createAnonymousAction.setEnabled(cls == null ? false : cls.isConcrete());
        
        Instance instance = (Instance) getSoleSelection();
        boolean allowed = instance != null && instance instanceof SimpleInstance;
        copyAction.setAllowed(allowed);
    }
}