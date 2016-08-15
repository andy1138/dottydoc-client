#!/bin/bash

if [ ! -d "scala-scala" ]; then
    git clone -b dotty-library git@github.com:DarkDimius/scala scala-scala;
fi
