package rabbitmqclient

import (
	_ "bytes"
	"converter/config"
	l "converter/logger"
	"github.com/liamylian/jsontime"

	amqp "github.com/rabbitmq/amqp091-go"
)

type PostMessage struct {
	Bucket      string
	Success     bool
	Rotation    string
	SourceFile  string
	Destination string
}

func InitRabbitmqClient() {
	// init logger
	var logger = l.NewLogger()

	pm := &PostMessage{
		Bucket:      "cnc-cm",
		Success:     true,
		Rotation:    "0",
		SourceFile:  "/tmp/data-cr2/test.cr2",
		Destination: "/tmp/data-tiff/magic_test.tiff",
	}

	logger.Info("pm : ", logger.PrettyGoStruct(*pm))
	publish(pm)
}

func publish(postMessage *PostMessage) {
	// custom json for all time formats
	var json = jsontime.ConfigWithCustomTimeFormat
	// init conf
	conf := config.GetConf()
	// init logger
	var logger = l.NewLogger()
	// get connection
	logger.Info("Abbout to connect to rabbitmq ...")
	conn, err := amqp.Dial(conf.RabbitMQ.GetConnection())
	failOnError(err, "Failed to connect to RabbitMQ")
	logger.Info("We are connected to RMQ!!!")
	// deffer connection till we are done
	defer conn.Close()
	// get a channel
	rabbitmqChannel, err := conn.Channel()
	failOnError(err, "Failed to open a channel")
	// deffer the channel till we are done
	defer rabbitmqChannel.Close()
	// declare the queue
	q, err := rabbitmqChannel.QueueDeclare(
		conf.RabbitMQ.GetQueueName(), // name
		true,                         // durable
		false,                        // delete when unused
		false,                        // exclusive
		false,                        // no-wait
		nil,                          // arguments
	)
	failOnError(err, "Failed to declare a queue")
	// prepare the body
	b, err := json.Marshal(postMessage)
	logger.Info("b : ", logger.PrettyGoStruct(b))
	failOnError(err, "Failed to json.Marshal postMessage")

	err = rabbitmqChannel.Publish(
		"",     // exchange
		q.Name, // routing key
		false,  // mandatory
		false,
		amqp.Publishing{
			DeliveryMode: amqp.Persistent,
			ContentType:  "application/json",
			Body:         b,
		})
	failOnError(err, "Failed to publish a message")

}

func failOnError(err error, msg string) {
	// init logger
	var logger = l.NewLogger()
	if err != nil {
		logger.Fatalf("%s: %s", msg, err)
	}
}
