# CSS Editor FX #

A CSS editor written in JavaFX 8

**This is a study project in 2017. Not maintain now.**

**Hope it inspires you about using Spring Boot + JavaFX together**

## Features ##
- [Code Highlight](#code-highlight)
- [Code Assist](#code-assist)
	- [JavaFX Context](#javafx-context)
	- [Current Context](#current-context)
	- *[Specify Context](#specify-context)*
- [Preview](#preview)
	- [Color](#color)
		- [RGB](#rgb)
		- [Color Function](#function)
		- [Gradient](#gradient)
		- *[Context Color Paser](#context-color-paser)*
	- [Shape](#shape)
- [Other Text Editor Common Features](#other)
	- *[Code Format](#code-format)*
	- [Toggle Comment](#toggle-comment)
	- [Custom Font](#custom-font)
	- [Custom Shortcut](#custom-shortcut)
- [Known Bugs](#known-bugs)

## Code Highlight ##
![highlight.png](readme/highlight.png)

## Code Assist ##
### JavaFX Context ###
![suggest-common.png](readme/suggest-common.png)
### Current Context ###
![suggest-current-id.png](readme/suggest-current-id.png)
### Specify Context ###
*Not implement yet*

## Preview ##
### Color ###
#### RGB ####
![color-color.png](readme/color-color.png)
#### Function ####
![color-derive.png](readme/color-derive.png)
#### Gradient ####
![color-lineargradient.png](readme/color-lineargradient.png)
#### Context Color Paser ####
![color-context.png](readme/color-context.png)
### Shape ###
![shape-svg.png](readme/shape-svg.png)

## Other ##
### Code Format ###
*Not implement yet*
### Toggle Comment ###

### Custom Font ###
![font.png](readme/font.png)
### Custom Shortcut ###
![key.png](readme/key.png)

## Known Bugs ##
- richtextfx codeArea undo leads IllegalArgumentException when merge changes. See [https://github.com/TomasMikula/RichTextFX/pull/402](https://github.com/TomasMikula/RichTextFX/pull/402 "github"), it may resolve soon.
- close tab will not correctly refresh currentTab property. It lead tab color and scrollbars still.