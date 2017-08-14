import matplotlib.pyplot as plt
import pandas as pd
import numpy as np

ts = pd.read_csv("G:\ForexPrediction\outputTest\outputValTrainFile")
check = pd.read_csv("G:\ForexPrediction\outputTest\outputValTestFile")
checkPlot = np.empty_like(ts)
checkPlot[:, : ] = np.nan
checkPlot[len(ts)-len(check):len(ts)] = check
plt.plot(ts)
plt.plot(checkPlot)
plt.show()