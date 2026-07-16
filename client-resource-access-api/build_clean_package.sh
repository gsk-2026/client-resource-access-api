#!/bin/bash

echo "======================================"
echo "DreamTech Client Resource Access API"
echo "======================================"

echo ""
echo "Cleaning the build..."
mvn clean

echo ""
echo "Packaging the application..."
if ! mvn package; then
    echo ""
    echo "***********************************"
    echo "BUILD FAILED"
    echo "***********************************"
    exit 1
fi

echo ""
echo "***********************************"
echo "BUILD SUCCESS"
echo "***********************************"