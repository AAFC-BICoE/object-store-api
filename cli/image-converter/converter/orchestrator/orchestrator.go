package orchestrator

import (
	"converter/config"
	l "converter/logger"
	"time"
)

func Run() {
	// time the start
	start := time.Now()
	// init logger
	var logger = l.NewLogger()
	// init conf
	conf := config.GetConf()

	// Bio cluster case only
	if conf.App.GetEnvironment() == "cluster" {
		logger.Info("Image-converter Orchestrator is about to run on the Bio cluster ...")
		logger.Info("Image-converter Orchestrator has finished the run on the Bio cluster !!!")
	}

	// TODO Development
	// Future development for for PC of scientists
	if conf.App.GetEnvironment() == "PC" {
		// TODO
		logger.Fatal("PC Run NOT SUPPORTED YET")
		// ...
		logger.Info("Image-converter Orchestrator is about to run on the PC ...")
		logger.Info("Image-converter Orchestrator has finished the run on the PC !!!")
	}

	// time the end
	t := time.Now()
	elapsed := t.Sub(start)
	logger.Debug("= = = = = = = = D O N E = = = = = = = =")
	logger.Debug("app Image-converter is done and it took : ", elapsed)
}
