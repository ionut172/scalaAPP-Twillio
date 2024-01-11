#!/bin/bash
sbt clean compile stage
cd target/universal
zip -r VoiceAPP.zip VoiceAPP
