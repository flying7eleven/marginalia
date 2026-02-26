package com.marginalia.wizard

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.GeneratorNewProjectWizard
import com.intellij.ide.wizard.NewProjectWizardChainStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.ide.wizard.RootNewProjectWizardStep
import javax.swing.Icon

class MarginaliaProjectWizard : GeneratorNewProjectWizard {

    override val id: String = "marginalia"

    override val name: String = "Marginalia"

    override val icon: Icon = MarginaliaIcons.Marginalia

    override fun createStep(wizardContext: WizardContext): NewProjectWizardStep =
        NewProjectWizardChainStep(RootNewProjectWizardStep(wizardContext))
            .nextStep(::MarginaliaProjectWizardStep)
}
