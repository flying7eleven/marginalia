package com.marginalia.scaffold

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SpecTemplatesTest {

    @Test
    fun `readme contains project name and description`() {
        val result = SpecTemplates.readme("MyApp", "A cool app")

        assertTrue(result.contains("MyApp"))
        assertTrue(result.contains("A cool app"))
        assertTrue(result.contains("# MyApp"))
    }

    @Test
    fun `readme contains repository structure`() {
        val result = SpecTemplates.readme("MyApp", "A cool app")

        assertTrue(result.contains("MyApp-specs/"))
        assertTrue(result.contains("product-description.md"))
        assertTrue(result.contains("epics/"))
        assertTrue(result.contains("stories/"))
    }

    @Test
    fun `productDescription contains project name and description`() {
        val result = SpecTemplates.productDescription("MyApp", "A cool app")

        assertTrue(result.contains("MyApp"))
        assertTrue(result.contains("A cool app"))
    }

    @Test
    fun `productDescription contains all required sections`() {
        val result = SpecTemplates.productDescription("MyApp", "A cool app")

        assertTrue(result.contains("## What is MyApp?"))
        assertTrue(result.contains("## Target Users"))
        assertTrue(result.contains("## Core Problem"))
        assertTrue(result.contains("## Key Features"))
        assertTrue(result.contains("## Success Criteria"))
        assertTrue(result.contains("## Constraints"))
    }

    @Test
    fun `epicsIndex contains correct table headers`() {
        val result = SpecTemplates.epicsIndex()

        assertTrue(result.contains("# Epics Index"))
        assertTrue(result.contains("| ID | Title | Status |"))
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `storiesIndex contains correct table headers`() {
        val result = SpecTemplates.storiesIndex()

        assertTrue(result.contains("# Stories Index"))
        assertTrue(result.contains("| ID | Title | Epic | Status |"))
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `all templates return non-empty strings`() {
        assertTrue(SpecTemplates.readme("X", "Y").isNotEmpty())
        assertTrue(SpecTemplates.productDescription("X", "Y").isNotEmpty())
        assertTrue(SpecTemplates.epicsIndex().isNotEmpty())
        assertTrue(SpecTemplates.storiesIndex().isNotEmpty())
    }
}
