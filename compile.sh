#!/bin/sh
javac -classpath .:classes:/opt/pi4j/lib/'*' -Dpi4j.linking=dynamic Main.java