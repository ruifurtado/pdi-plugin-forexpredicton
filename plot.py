import matplotlib.pyplot as plt
import pandas as pd
import numpy as np

trainPath = input("Specify the path of the training file: ")
trainDel = input("Specify the train file delimeter: ")
testPath = input("Specify the path of the output file: ")
testDel = input("Specify the output file delimeter: ")

train = pd.read_csv(trainPath,",",usecols=[1], engine='python')
evaluationAndTest = pd.read_csv(testPath,";",usecols=[1], engine='python')


checkLength=round(len(evaluationAndTest)/2)

evaluation = pd.DataFrame()
test = pd.DataFrame()

evaluation = evaluationAndTest[:checkLength]
test = evaluationAndTest[checkLength:]

evalPlot = np.empty_like(train)
evalPlot[:, : ] = np.nan
evalPlot[len(train)-len(evaluation):len(train)] = evaluation

testPlot=np.append(evalPlot,test,axis=0)

plt.plot(train)
plt.plot(testPlot)
plt.plot(evalPlot)

plt.show()