package com.fluxtion.example.servicestater.helpers;

import com.fluxtion.example.servicestater.TaskWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SynchronousTaskExecutor implements TaskWrapper.TaskExecutor {

    @Override
    public void close() {
        //do nothing
    }

    @Override
    public void accept(List<TaskWrapper> taskWrapper) {
            taskWrapper.forEach(TaskWrapper::call);
    }

}
