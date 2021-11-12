package execmagic

import (
	l "converter/logger"
	"os/exec"
)

func Convert(rotation string, sourcePath string, destinationPath string) {
	// init logger
	var logger = l.NewLogger()

	logger.Info("Converting source:", sourcePath)
	logger.Info("Converting destination:", destinationPath)
	logger.Info("rotation : ", rotation)

	cmd := exec.Command("/app/magic.sh", rotation, sourcePath, destinationPath)
	logger.Info("Running command and waiting for it to finish...")
	err := cmd.Run()
	logger.Info("Command finished with error:", err)

	logger.Info("Converting from CR2 to TIFF is done !!!")
}
