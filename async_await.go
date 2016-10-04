package main

import (
	"fmt"
	"time"
)

// Task definition
type Task struct {
	Channel chan interface{}
	Result  interface{}
	Error   error
}

func Async(fn func() interface{}) *Task {
	t := new(Task)
	t.Channel = make(chan interface{})

	go func() {
		defer func() {
			if err := recover(); err != nil {
				t.Error = err.(error)
			}

			t.Channel <- t.Result
		}()

		t.Result = fn()
	}()

	return t
}

func Await(t *Task) (interface{}, error) {
	t.Result = <-t.Channel
	return t.Result, t.Error
}

// ahahahahah
func sleepAsync(ms int64) *Task {
	return Async(func() interface{} {
		time.Sleep(time.Millisecond * time.Duration(ms))
		return nil
	})
}

func testAsync() {
	fmt.Printf("SOmething here\n")
	Await(sleepAsync(1000))
	fmt.Printf("And it's done \n")
}

func handleSleepAsync(task *Task) {
	go func() {
		time.Sleep(time.Millisecond * 1000)
		task.Result = "nothing"
		task.Channel <- task.Result
		return
	}()
}

// Await functionally
func testAsync2() {
	var task = new(Task)
	task.Channel = make(chan interface{}, 1)
	fmt.Printf("Do something\n")
	handleSleepAsync(task)
	Await(task)
	fmt.Printf("Do another %v\n", task.Result)
}

func main() {
	testAsync2()
}
