package logger

import (
	"converter/config"
	"github.com/sirupsen/logrus"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestLoggerNoConfigFile(t *testing.T) {
	// init conf
	conf := config.GetConf()
	assert.NotNil(t, conf)
	// init logger
	var logger = NewLogger()
	// check if the logger has been init
	assert.NotNil(t, logger)
	// testing idefault log level
	assert.Equal(t, logger.GetLevel(), logrus.InfoLevel)
}

func TestLoggerInit(t *testing.T) {
	// load config from file
	file := "../converter_config.yml"
	config.Load(file)
	// init logger
	var logger = NewLogger()
	// check if the logger has been init
	assert.NotNil(t, logger)
}

func TestLoggerInfo(t *testing.T) {
	// load config from file
	file := "../converter_config.yml"
	config.Load(file)
	// init logger
	var logger = NewLogger()
	// testing log level
	assert.Equal(t, logger.GetLevel(), logrus.InfoLevel)
}

func TestLoggerDebug(t *testing.T) {
	// load config from file
	file := "../converter_config.yml"
	config.Load(file)
	// init logger
	var logger = NewLogger()
	// testing Debug
	logger.Debug(logger.PrettyGoStruct(config.GetConf()))
	assert.NotNil(t, logger)
}
