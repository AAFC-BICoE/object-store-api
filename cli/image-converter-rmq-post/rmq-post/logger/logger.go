// This is out of the box common logger from logrus
// All logs will be stored as JSON for better searching
// TODO Need to check later on if this app needs
// error handling common interfaces or it's an over kill
// For now just keeping it simple

package logger

import (
	"converter/config"
	"encoding/json"
	rotatelogs "github.com/lestrrat-go/file-rotatelogs"
	"github.com/rifflock/lfshook"
	"github.com/sirupsen/logrus"
	"os"
	"time"
)

// StandardLogger enforces specific log message formats
type StandardLogger struct {
	*logrus.Logger
}

// init global instance
var standardLogger *StandardLogger

// Map log level from config
var Levels = map[string]logrus.Level{
	"info":  logrus.InfoLevel,
	"debug": logrus.DebugLevel,
	"trace": logrus.TraceLevel,
	"warn":  logrus.WarnLevel,
	"error": logrus.ErrorLevel,
	"fatal": logrus.FatalLevel,
	"panic": logrus.PanicLevel,
}

// NewLogger initializes the standard logger
func NewLogger() *StandardLogger {
	if standardLogger != nil {
		return standardLogger
	}

	// new base logrus
	var baseLogger = logrus.New()
	// introducing our standard logger from base logrus logger
	standardLogger = &StandardLogger{baseLogger}
	// getting log level from our config
	conf := config.GetConf()
	// getting log level from config
	l := conf.Logger.GetLevel()
	// if level is set correctly then setting ...
	level, ok := Levels[l]
	if ok {
		standardLogger.SetLevel(level)
	} else {
		// fallback in case of an error in the config to Info
		standardLogger.SetLevel(logrus.InfoLevel)
	}

	// define path to the file from config
	path := conf.Logger.GetPath() + string(os.PathSeparator) + conf.Logger.GetFile()
	// setting log rotate
	writer, err := rotatelogs.New(
		path+".%Y%m%d%H%M",
		rotatelogs.WithLinkName(path),
		rotatelogs.WithRotationTime(time.Hour*24), // rotate each day
		rotatelogs.WithMaxAge(-1),                 // reset max age due to count
		rotatelogs.WithRotationCount(7),           // keep logs for 1 week top
	)

	// check if the writer is ok for the log file
	if err != nil {
		logrus.Fatal(" Can't create rotatelogs ", path, " | ", err)
	}

	// need only the following levels in the log file
	standardLogger.Hooks.Add(lfshook.NewHook(
		lfshook.WriterMap{
			logrus.InfoLevel:  writer,
			logrus.WarnLevel:  writer,
			logrus.ErrorLevel: writer,
			logrus.FatalLevel: writer,
			logrus.PanicLevel: writer,
		},
		&logrus.JSONFormatter{
			// for debug only
			// make it to true if it's hard to see in json logs
			PrettyPrint:     false,
			TimestampFormat: time.RFC822,
		},
	))

	// SetOutput
	standardLogger.SetOutput(os.Stdout)
	standardLogger.SetFormatter(&logrus.TextFormatter{
		ForceColors:     true,
		FullTimestamp:   true,
		TimestampFormat: time.RFC822,
	})

	// some bonus for Debug
	if standardLogger.GetLevel() == logrus.DebugLevel {
		baseLogger.SetReportCaller(true)
	}

	return standardLogger
}

// Helper function
// Please use it only for Debug !!!
func (s *StandardLogger) PrettyGoStruct(uglyStruct interface{}) string {
	prettyStruct, _ := json.MarshalIndent(uglyStruct, "", "\t")
	return string(prettyStruct)
}
